package com.pantrypal.service;

import com.pantrypal.config.PantryPalProperties;
import com.pantrypal.domain.Category;
import com.pantrypal.engine.IngredientNames;
import com.pantrypal.engine.PantrySnapshot;
import com.pantrypal.engine.StockItem;
import com.pantrypal.engine.WasteRiskCalculator;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.UsageLog;
import com.pantrypal.model.User;
import com.pantrypal.repository.IngredientRepository;
import com.pantrypal.repository.UsageLogRepository;
import com.pantrypal.repository.UserRepository;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.PantryDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Digital pantry: CRUD, search, use/discard and waste-risk ranking. Every query is scoped to one user. */
@Service
public class PantryService {

    private final IngredientRepository ingredients;
    private final UserRepository users;
    private final UsageLogRepository usage;
    private final PriceEstimator prices;
    private final PantryPalProperties props;
    private final CurrentTime clock;

    public PantryService(IngredientRepository ingredients, UserRepository users, UsageLogRepository usage,
                         PriceEstimator prices, PantryPalProperties props, CurrentTime clock) {
        this.ingredients = ingredients;
        this.users = users;
        this.usage = usage;
        this.prices = prices;
        this.props = props;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PantryDtos.IngredientDto> list(Long userId, String query, Category category) {
        String pattern = query == null || query.isBlank() ? null : "%" + query.trim().toLowerCase() + "%";
        int threshold = threshold(userId);
        LocalDate today = clock.today();
        return ingredients.search(userId, category, pattern).stream().map(i -> toDto(i, today, threshold)).toList();
    }

    @Transactional(readOnly = true)
    public List<Ingredient> entities(Long userId) {
        return ingredients.findByUserIdOrderByExpirationDateAsc(userId);
    }

    @Transactional(readOnly = true)
    public PantrySnapshot snapshot(Long userId) {
        return EngineMapper.snapshot(entities(userId), clock.today());
    }

    /** Pantry items ordered by waste risk, highest first ("use these first"). */
    @Transactional(readOnly = true)
    public List<PantryDtos.IngredientDto> byWasteRisk(Long userId) {
        int threshold = threshold(userId);
        LocalDate today = clock.today();
        return entities(userId).stream()
                .map(i -> toDto(i, today, threshold))
                .sorted(Comparator.comparingInt(PantryDtos.IngredientDto::wasteRisk).reversed()
                        .thenComparing(PantryDtos.IngredientDto::expirationDate))
                .toList();
    }

    @Transactional
    public PantryDtos.IngredientDto create(Long userId, PantryDtos.IngredientRequest req) {
        if (ingredients.countByUserId(userId) >= props.getMaxPantryItems()) {
            throw ApiException.badRequest("Your pantry is full (" + props.getMaxPantryItems()
                    + " items). Use or remove something first.");
        }
        if (req.expirationDate().isBefore(clock.today())) {
            throw ApiException.badRequest("Invalid date: the expiration date is in the past. Please pick today or a future date.");
        }
        User user = users.getReferenceById(userId);
        Ingredient i = new Ingredient(user, clean(req.name()), req.category(), req.quantity(), req.unit(), req.expirationDate());
        return toDto(ingredients.save(i), clock.today(), threshold(userId));
    }

    @Transactional
    public PantryDtos.IngredientDto update(Long userId, Long id, PantryDtos.IngredientRequest req) {
        Ingredient i = own(userId, id);
        if (!req.expirationDate().equals(i.getExpirationDate()) && req.expirationDate().isBefore(clock.today())) {
            throw ApiException.badRequest("Invalid date: the expiration date is in the past.");
        }
        i.setName(clean(req.name()));
        i.setCategory(req.category());
        i.setQuantity(req.quantity());
        i.setUnit(req.unit());
        i.setExpirationDate(req.expirationDate());
        return toDto(i, clock.today(), threshold(userId));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ingredients.delete(own(userId, id));
    }

    /** Records that some of an item was eaten/cooked. Rescued = used within the "expiring soon" window. */
    @Transactional
    public PantryDtos.ActionResult use(Long userId, Long id, double amount) {
        Ingredient i = own(userId, id);
        if (amount > i.getQuantity() + 1e-9) {
            throw ApiException.badRequest("You only have " + fmt(i.getQuantity()) + " " + i.getUnit().getSymbol() + " of " + i.getName() + ".");
        }
        LocalDate today = clock.today();
        int threshold = threshold(userId);
        long days = WasteRiskCalculator.daysLeft(i.getExpirationDate(), today);
        boolean expired = days < 0;
        boolean rescued = !expired && days <= threshold;
        double value = prices.estimate(i.getCategory(), amount, i.getUnit());
        usage.save(new UsageLog(i.getUser(), i.getName(), i.getCategory(), amount, i.getUnit(),
                UsageLog.Action.USED, rescued, value));

        double left = i.getQuantity() - amount;
        PantryDtos.IngredientDto dto = null;
        if (left <= 1e-6) ingredients.delete(i);
        else {
            i.setQuantity(UnitRound.round(left));
            dto = toDto(i, today, threshold);
        }
        String msg = rescued ? "Nice work! You saved food from going to waste!"
                : dto == null ? "All used up! " + i.getName() + " has left the pantry." : "Updated! Enjoy your " + i.getName() + ".";
        return new PantryDtos.ActionResult(dto, msg, rescued, value);
    }

    /** Throws an item away and logs it as waste, so the waste-reduction progress stays honest. */
    @Transactional
    public PantryDtos.ActionResult discard(Long userId, Long id) {
        Ingredient i = own(userId, id);
        double value = prices.estimate(i.getCategory(), i.getQuantity(), i.getUnit());
        usage.save(new UsageLog(i.getUser(), i.getName(), i.getCategory(), i.getQuantity(), i.getUnit(),
                UsageLog.Action.WASTED, false, value));
        ingredients.delete(i);
        return new PantryDtos.ActionResult(null, "Removed. We'll help you catch it earlier next time!", false, 0);
    }

    /** Adds stock (used when shopping items are moved into the pantry). */
    @Transactional
    public Ingredient addStock(User user, String name, Category category, double quantity, com.pantrypal.domain.Unit unit,
                               LocalDate expiry) {
        if (ingredients.countByUserId(user.getId()) >= props.getMaxPantryItems()) {
            throw ApiException.badRequest("Your pantry is full (" + props.getMaxPantryItems() + " items).");
        }
        return ingredients.save(new Ingredient(user, clean(name), category, quantity, unit, expiry));
    }

    public Ingredient own(Long userId, Long id) {
        return ingredients.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Pantry item"));
    }

    public int threshold(Long userId) {
        return users.findById(userId).map(User::getExpiryThresholdDays).orElse(props.getDefaultThresholdDays());
    }

    public static PantryDtos.IngredientDto toDto(Ingredient i, LocalDate today, int threshold) {
        StockItem s = EngineMapper.toStock(i);
        long days = WasteRiskCalculator.daysLeft(i.getExpirationDate(), today);
        return new PantryDtos.IngredientDto(i.getId(), i.getName(), i.getCategory(), i.getCategory().getLabel(),
                i.getQuantity(), i.getUnit(), i.getUnit().getSymbol(), i.getExpirationDate(), days,
                WasteRiskCalculator.level(i.getExpirationDate(), today, threshold),
                WasteRiskCalculator.score(s, today), i.getUser().getId());
    }

    private static String clean(String name) {
        return IngredientNames.display(name.trim().replaceAll("\\s+", " "));
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(UnitRound.round(v));
    }

    /** Keeps quantities tidy after subtraction (0.30000000004 -> 0.3). */
    static final class UnitRound {
        static double round(double v) { return Math.round(v * 1000.0) / 1000.0; }
    }
}
