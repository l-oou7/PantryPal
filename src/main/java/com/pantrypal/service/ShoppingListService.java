package com.pantrypal.service;

import com.pantrypal.domain.Category;
import com.pantrypal.engine.*;
import com.pantrypal.model.*;
import com.pantrypal.repository.*;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.ShoppingDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/** Automatic shopping list generation from the meal plan, plus manual list editing. */
@Service
public class ShoppingListService {

    /** Default shelf life when bought items are moved into the pantry. */
    private static final Map<Category, Integer> SHELF_LIFE_DAYS = Map.of(
            Category.FRUITS, 7, Category.VEGETABLES, 7, Category.DAIRY, 7, Category.MEAT_SEAFOOD, 3,
            Category.GRAINS, 180, Category.SPICES, 365, Category.CANNED_GOODS, 365, Category.OTHERS, 30);

    private final ShoppingListRepository lists;
    private final ShoppingListItemRepository items;
    private final MealPlanRepository mealPlans;
    private final RecipeRepository recipes;
    private final UserRepository users;
    private final PantryService pantry;
    private final CurrentTime clock;

    public ShoppingListService(ShoppingListRepository lists, ShoppingListItemRepository items,
                               MealPlanRepository mealPlans, RecipeRepository recipes, UserRepository users,
                               PantryService pantry, CurrentTime clock) {
        this.lists = lists;
        this.items = items;
        this.mealPlans = mealPlans;
        this.recipes = recipes;
        this.users = users;
        this.pantry = pantry;
        this.clock = clock;
    }

    @Transactional
    public ShoppingDtos.ListDto get(Long userId) {
        return toDto(listFor(userId));
    }

    /**
     * Rebuilds the automatic part of the list from planned (not yet cooked) meals between from and to.
     * Manual items and items already ticked off are kept.
     */
    @Transactional
    public ShoppingDtos.GenerateResult generate(Long userId, LocalDate from, LocalDate to) {
        LocalDate start = from == null ? clock.today() : from;
        LocalDate end = to == null ? MealPlanService.weekStartOf(start).plusDays(6) : to;
        if (end.isBefore(start)) throw ApiException.badRequest("The end date is before the start date.");
        if (start.plusDays(62).isBefore(end)) throw ApiException.badRequest("Please pick a range of two months or less.");

        List<MealPlan> plan = mealPlans.findRange(userId, start, end).stream().filter(m -> !m.isCooked()).toList();
        Map<Long, Recipe> byId = new HashMap<>();
        recipes.findByIdIn(plan.stream().map(m -> m.getRecipe().getId()).distinct().toList())
                .forEach(r -> byId.put(r.getId(), r));
        List<ShoppingCalculator.Planned> planned = plan.stream()
                .map(m -> new ShoppingCalculator.Planned(EngineMapper.toSpec(byId.get(m.getRecipe().getId())), m.getServings()))
                .toList();

        PantrySnapshot snap = pantry.snapshot(userId);
        List<ShoppingCalculator.Need> needs = ShoppingCalculator.calculate(planned, snap);
        long distinctRequired = planned.stream().flatMap(p -> p.recipe().requiredLines().stream())
                .map(l -> l.key() + "|" + l.unit().getDimension()).distinct().count();

        ShoppingList list = listFor(userId);
        list.getItems().removeIf(i -> i.getSource() == ShoppingListItem.Source.AUTO && !i.isPurchased());
        int added = 0;
        for (ShoppingCalculator.Need n : needs) {
            Quantity q = n.toBuy();
            String note = "For " + String.join(", ", n.forRecipes()) + " · need " + n.required().format()
                    + (n.inPantry().amount() > 0 ? ", have " + n.inPantry().format() : "");
            list.addItem(new ShoppingListItem(n.name(), q.amount(), q.unit(), n.category(),
                    ShoppingListItem.Source.AUTO, truncate(note, 400)));
            added++;
        }
        list.setGeneratedRange(start, end);
        lists.saveAndFlush(list); // flush so new items have ids in the response

        int covered = (int) Math.max(0, distinctRequired - needs.size());
        String msg = plan.isEmpty() ? "No meals planned in that range yet. Plan a few meals first!"
                : added == 0 ? "Your pantry already covers every planned meal. Nothing to buy!"
                : "List ready! " + added + " item" + (added == 1 ? "" : "s") + " to buy, " + covered + " covered by your pantry.";
        return new ShoppingDtos.GenerateResult(toDto(list), added, plan.size(), covered, msg);
    }

    /** Adds an item by hand; merges into an existing unticked line with the same name and a compatible unit. */
    @Transactional
    public ShoppingDtos.ListDto addItem(Long userId, ShoppingDtos.ItemRequest req) {
        ShoppingList list = listFor(userId);
        String key = IngredientNames.normalize(req.name());
        for (ShoppingListItem i : list.getItems()) {
            if (!i.isPurchased() && IngredientNames.normalize(i.getName()).equals(key)
                    && UnitNormalizer.compatible(i.getUnit(), req.unit())) {
                double base = UnitNormalizer.toBase(i.getQuantity(), i.getUnit()) + UnitNormalizer.toBase(req.quantity(), req.unit());
                Quantity merged = UnitNormalizer.humanize(base, req.unit().getDimension());
                i.setQuantity(merged.amount());
                i.setUnit(merged.unit());
                list.touch();
                return toDto(list);
            }
        }
        list.addItem(new ShoppingListItem(IngredientNames.display(req.name().trim()), req.quantity(), req.unit(),
                req.category() == null ? Category.OTHERS : req.category(), ShoppingListItem.Source.MANUAL, null));
        lists.saveAndFlush(list);
        return toDto(list);
    }

    @Transactional
    public ShoppingDtos.ListDto updateItem(Long userId, Long itemId, ShoppingDtos.ItemRequest req) {
        ShoppingListItem i = own(userId, itemId);
        i.setName(IngredientNames.display(req.name().trim()));
        i.setQuantity(req.quantity());
        i.setUnit(req.unit());
        if (req.category() != null) i.setCategory(req.category());
        if (req.purchased() != null) i.setPurchased(req.purchased());
        i.getShoppingList().touch();
        return toDto(i.getShoppingList());
    }

    @Transactional
    public ShoppingDtos.ListDto setPurchased(Long userId, Long itemId, boolean purchased) {
        ShoppingListItem i = own(userId, itemId);
        i.setPurchased(purchased);
        i.getShoppingList().touch();
        return toDto(i.getShoppingList());
    }

    @Transactional
    public ShoppingDtos.ListDto deleteItem(Long userId, Long itemId) {
        ShoppingListItem i = own(userId, itemId);
        ShoppingList list = i.getShoppingList();
        list.getItems().remove(i);
        list.touch();
        return toDto(list);
    }

    @Transactional
    public ShoppingDtos.ListDto clearPurchased(Long userId) {
        ShoppingList list = listFor(userId);
        list.getItems().removeIf(ShoppingListItem::isPurchased);
        list.touch();
        return toDto(list);
    }

    /** Moves ticked-off items into the pantry with a typical shelf life for their category. */
    @Transactional
    public ShoppingDtos.ListDto movePurchasedToPantry(Long userId) {
        ShoppingList list = listFor(userId);
        User user = users.getReferenceById(userId);
        List<ShoppingListItem> bought = list.getItems().stream().filter(ShoppingListItem::isPurchased).toList();
        if (bought.isEmpty()) throw ApiException.badRequest("Tick off the items you bought first.");
        for (ShoppingListItem i : bought) {
            int days = SHELF_LIFE_DAYS.getOrDefault(i.getCategory(), 14);
            pantry.addStock(user, i.getName(), i.getCategory(), i.getQuantity(), i.getUnit(), clock.today().plusDays(days));
        }
        list.getItems().removeAll(bought);
        list.touch();
        return toDto(list);
    }

    private ShoppingListItem own(Long userId, Long itemId) {
        return items.findByIdAndShoppingListUserId(itemId, userId).orElseThrow(() -> ApiException.notFound("Shopping item"));
    }

    private ShoppingList listFor(Long userId) {
        return lists.findByUserId(userId).orElseGet(() -> lists.save(new ShoppingList(users.getReferenceById(userId))));
    }

    private static ShoppingDtos.ListDto toDto(ShoppingList l) {
        List<ShoppingDtos.ItemDto> dtos = l.getItems().stream()
                .sorted(Comparator.comparing(ShoppingListItem::isPurchased)
                        .thenComparing(i -> i.getCategory().ordinal())
                        .thenComparing(ShoppingListItem::getName))
                .map(i -> new ShoppingDtos.ItemDto(i.getId(), i.getName(), i.getQuantity(), i.getUnit(),
                        i.getUnit().getSymbol(), i.getCategory(), i.getCategory().getLabel(), i.isPurchased(),
                        i.getSource().name(), i.getNote()))
                .toList();
        int bought = (int) dtos.stream().filter(ShoppingDtos.ItemDto::purchased).count();
        return new ShoppingDtos.ListDto(l.getId(), l.getName(), l.getGeneratedFrom(), l.getGeneratedTo(), l.getUpdatedAt(),
                dtos, dtos.size() - bought, bought);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

}
