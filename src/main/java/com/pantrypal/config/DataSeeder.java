package com.pantrypal.config;

import com.pantrypal.domain.*;
import com.pantrypal.model.*;
import com.pantrypal.repository.*;
import com.pantrypal.service.CurrentTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

/**
 * Populates an empty database with the recipe catalogue and two demo accounts so every feature
 * can be tried immediately. Pantry dates are relative to today so expiry alerts always show up.
 * <pre>
 *   demo@pantrypal.app     / Demo1234   (general diet, well-stocked pantry, meals planned)
 *   student@pantrypal.app  / Student123 (vegan, nut allergy, small pantry)
 * </pre>
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RecipeRepository recipes;
    private final UserRepository users;
    private final IngredientRepository ingredients;
    private final MealPlanRepository mealPlans;
    private final RecipeBookmarkRepository bookmarks;
    private final UsageLogRepository usage;
    private final PasswordEncoder encoder;
    private final CurrentTime clock;

    @Value("${pantrypal.seed.enabled:true}")
    private boolean enabled;

    public DataSeeder(RecipeRepository recipes, UserRepository users, IngredientRepository ingredients,
                      MealPlanRepository mealPlans, RecipeBookmarkRepository bookmarks, UsageLogRepository usage,
                      PasswordEncoder encoder, CurrentTime clock) {
        this.recipes = recipes;
        this.users = users;
        this.ingredients = ingredients;
        this.mealPlans = mealPlans;
        this.bookmarks = bookmarks;
        this.usage = usage;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        int added = 0;
        for (Recipe r : SeedRecipes.all()) {
            if (!recipes.existsByNameIgnoreCase(r.getName())) {
                recipes.save(r);
                added++;
            }
        }
        if (added > 0) log.info("Seeded {} recipes", added);
        if (!users.existsByEmailIgnoreCase("demo@pantrypal.app")) seedDemo();
        if (!users.existsByEmailIgnoreCase("student@pantrypal.app")) seedStudent();
    }

    private void seedDemo() {
        User u = new User("Alex Demo", "demo@pantrypal.app", encoder.encode("Demo1234"));
        u.setDietaryPreference(new DietaryPreference(DietType.GENERAL));
        users.save(u);
        LocalDate t = clock.today();
        List<Ingredient> items = List.of(
                new Ingredient(u, "Pasta", Category.GRAINS, 500, Unit.G, t.plusDays(200)),
                new Ingredient(u, "Tomato", Category.VEGETABLES, 4, Unit.PCS, t.plusDays(2)),
                new Ingredient(u, "Garlic", Category.VEGETABLES, 8, Unit.PCS, t.plusDays(20)),
                new Ingredient(u, "Olive oil", Category.OTHERS, 0.75, Unit.L, t.plusDays(300)),
                new Ingredient(u, "Milk", Category.DAIRY, 1, Unit.L, t.plusDays(1)),
                new Ingredient(u, "Egg", Category.DAIRY, 6, Unit.PCS, t.plusDays(10)),
                new Ingredient(u, "Spinach", Category.VEGETABLES, 150, Unit.G, t),
                new Ingredient(u, "Mushroom", Category.VEGETABLES, 250, Unit.G, t.plusDays(3)),
                new Ingredient(u, "Butter", Category.DAIRY, 200, Unit.G, t.plusDays(25)),
                new Ingredient(u, "Bread", Category.GRAINS, 6, Unit.PCS, t.plusDays(2)),
                new Ingredient(u, "Banana", Category.FRUITS, 3, Unit.PCS, t.plusDays(1)),
                new Ingredient(u, "Onion", Category.VEGETABLES, 3, Unit.PCS, t.plusDays(30)),
                new Ingredient(u, "Rice", Category.GRAINS, 1, Unit.KG, t.plusDays(365)),
                new Ingredient(u, "Flour", Category.GRAINS, 1, Unit.KG, t.plusDays(180)),
                new Ingredient(u, "Sugar", Category.OTHERS, 500, Unit.G, t.plusDays(365)),
                new Ingredient(u, "Cinnamon", Category.SPICES, 30, Unit.G, t.plusDays(400)),
                new Ingredient(u, "Chicken breast", Category.MEAT_SEAFOOD, 400, Unit.G, t.plusDays(2)),
                new Ingredient(u, "Feta", Category.DAIRY, 150, Unit.G, t.plusDays(6)),
                new Ingredient(u, "Yogurt", Category.DAIRY, 500, Unit.G, t.minusDays(1)),
                new Ingredient(u, "Carrot", Category.VEGETABLES, 5, Unit.PCS, t.plusDays(12)),
                new Ingredient(u, "Soy sauce", Category.SPICES, 250, Unit.ML, t.plusDays(300)),
                new Ingredient(u, "Vegetable broth", Category.CANNED_GOODS, 1, Unit.L, t.plusDays(120)));
        ingredients.saveAll(items);

        // a little history so the dashboard progress isn't empty
        usage.save(new UsageLog(u, "Bread", Category.GRAINS, 4, Unit.PCS, UsageLog.Action.USED, true, 12.0));
        usage.save(new UsageLog(u, "Tomato", Category.VEGETABLES, 3, Unit.PCS, UsageLog.Action.USED, true, 3.6));
        usage.save(new UsageLog(u, "Rice", Category.GRAINS, 300, Unit.G, UsageLog.Action.USED, false, 2.4));
        usage.save(new UsageLog(u, "Lettuce", Category.VEGETABLES, 1, Unit.PCS, UsageLog.Action.WASTED, false, 1.2));

        plan(u, t, MealType.DINNER, "Vegetable Pasta", 2);
        plan(u, t.plusDays(1), MealType.BREAKFAST, "Fluffy Pancakes", 3);
        plan(u, t.plusDays(1), MealType.LUNCH, "Spinach & Feta Omelette", 1);
        plan(u, t.plusDays(2), MealType.DINNER, "Simple Chicken Curry", 4);
        plan(u, t.plusDays(3), MealType.DINNER, "Mushroom Risotto", 3);

        recipes.findAllByOrderByNameAsc().stream()
                .filter(r -> r.getName().equals("Banana Bread") || r.getName().equals("Shakshuka"))
                .forEach(r -> bookmarks.save(new RecipeBookmark(u, r)));
        log.info("Created demo account demo@pantrypal.app / Demo1234");
    }

    private void seedStudent() {
        User u = new User("Sam Student", "student@pantrypal.app", encoder.encode("Student123"));
        DietaryPreference p = new DietaryPreference(DietType.VEGAN);
        p.setAllergies(EnumSet.of(Allergen.PEANUTS));
        u.setDietaryPreference(p);
        users.save(u);
        LocalDate t = clock.today();
        ingredients.saveAll(List.of(
                new Ingredient(u, "Rice", Category.GRAINS, 2, Unit.KG, t.plusDays(300)),
                new Ingredient(u, "Chickpea", Category.CANNED_GOODS, 800, Unit.G, t.plusDays(400)),
                new Ingredient(u, "Onion", Category.VEGETABLES, 2, Unit.PCS, t.plusDays(14)),
                new Ingredient(u, "Garlic", Category.VEGETABLES, 5, Unit.PCS, t.plusDays(20)),
                new Ingredient(u, "Curry powder", Category.SPICES, 50, Unit.G, t.plusDays(200)),
                new Ingredient(u, "Spinach", Category.VEGETABLES, 200, Unit.G, t.plusDays(1)),
                new Ingredient(u, "Avocado", Category.FRUITS, 2, Unit.PCS, t.plusDays(2)),
                new Ingredient(u, "Bread", Category.GRAINS, 8, Unit.PCS, t.plusDays(4))));
        log.info("Created demo account student@pantrypal.app / Student123");
    }

    private void plan(User u, LocalDate date, MealType type, String recipeName, int servings) {
        recipes.findAllByOrderByNameAsc().stream().filter(r -> r.getName().equals(recipeName)).findFirst()
                .ifPresent(r -> {
                    if (mealPlans.findByUserIdAndPlanDateAndMealType(u.getId(), date, type).isEmpty()) {
                        mealPlans.save(new MealPlan(u, date, type, r, servings));
                    }
                });
    }
}
