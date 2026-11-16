package com.pantrypal.engine;

import com.pantrypal.domain.*;
import com.pantrypal.engine.strategy.StrategyRegistry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Plain unit tests of the recommendation engine. No Spring context or database needed. */
class RecommendationEngineTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 1);
    private final SubstitutionEngine subs = new SubstitutionEngine();
    private final RecommendationEngine engine = new RecommendationEngine(new MatchCalculator(subs), new StrategyRegistry());
    private final RecommendationContext general = new RecommendationContext(DietType.GENERAL, Set.of(), 3, TODAY);

    private static RecipeLine line(String n, double a, Unit u, Category c) { return new RecipeLine(n, a, u, c, false); }

    private static StockItem stock(long id, String n, double q, Unit u, Category c, int daysLeft) {
        return new StockItem(id, n, c, q, u, TODAY.plusDays(daysLeft));
    }

    private static RecipeSpec vegetablePasta() {
        return new RecipeSpec(1, "Vegetable Pasta", DietType.VEGETARIAN, Course.MAIN, 2, List.of(
                line("Pasta", 200, Unit.G, Category.GRAINS),
                line("Tomatoes", 3, Unit.PCS, Category.VEGETABLES),
                line("Garlic", 2, Unit.PCS, Category.VEGETABLES),
                line("Olive oil", 2, Unit.TBSP, Category.OTHERS),
                line("Cheese", 50, Unit.G, Category.DAIRY)));
    }

    private PantrySnapshot pantry(StockItem... items) { return new PantrySnapshot(List.of(items), TODAY); }

    @Test
    void documentExampleGivesEightyPercent() {
        PantrySnapshot p = pantry(
                stock(1, "pasta", 0.5, Unit.KG, Category.GRAINS, 100),
                stock(2, "tomato", 4, Unit.PCS, Category.VEGETABLES, 5),
                stock(3, "garlic", 5, Unit.PCS, Category.VEGETABLES, 20),
                stock(4, "olive oil", 1, Unit.L, Category.OTHERS, 300));
        RecipeMatch m = engine.evaluate(vegetablePasta(), p, general, null);
        assertEquals(80, m.matchPercent());
        assertEquals(4, m.fullyAvailable());
        assertEquals(1, m.missing());
        assertFalse(m.canCookNow());
    }

    @Test
    void quantityIsConsideredNotJustNames() {
        PantrySnapshot p = pantry(
                stock(1, "pasta", 100, Unit.G, Category.GRAINS, 100),          // half of the 200 g needed
                stock(2, "tomato", 4, Unit.PCS, Category.VEGETABLES, 5),
                stock(3, "garlic", 5, Unit.PCS, Category.VEGETABLES, 20),
                stock(4, "olive oil", 1, Unit.L, Category.OTHERS, 300));
        RecipeMatch m = engine.evaluate(vegetablePasta(), p, general, null);
        assertEquals(70, m.matchPercent()); // (0.5 + 3) / 5
        LineAssessment pasta = m.lines().get(0);
        assertEquals(IngredientStatus.NEED_MORE, pasta.status());
        assertEquals("100 g", pasta.shortfall().format());
    }

    @Test
    void servingsScaleRequirements() {
        PantrySnapshot p = pantry(stock(1, "pasta", 300, Unit.G, Category.GRAINS, 100));
        RecipeMatch two = engine.evaluate(vegetablePasta(), p, general, 2);
        RecipeMatch four = engine.evaluate(vegetablePasta(), p, general, 4);
        assertEquals(IngredientStatus.HAVE_ENOUGH, two.lines().get(0).status());
        assertEquals(IngredientStatus.NEED_MORE, four.lines().get(0).status()); // needs 400 g
    }

    @Test
    void expiredStockIsNotAvailable() {
        PantrySnapshot p = pantry(stock(1, "Cheese", 200, Unit.G, Category.DAIRY, -1));
        assertFalse(p.has("cheese"));
    }

    @Test
    void unitsAreNormalised() {
        assertEquals(1500, UnitNormalizer.convert(1.5, Unit.KG, Unit.G), 1e-9);
        assertEquals(0.25, UnitNormalizer.convert(250, Unit.ML, Unit.L), 1e-9);
        assertEquals("1.5 kg", UnitNormalizer.humanize(1500, Dimension.MASS).format());
        assertThrows(IllegalArgumentException.class, () -> UnitNormalizer.convert(1, Unit.KG, Unit.L));
    }

    @Test
    void namesAreNormalised() {
        assertEquals("tomato", IngredientNames.normalize("  Tomatoes "));
        assertEquals("tomato", IngredientNames.normalize("cherry tomatoes"));
        assertEquals("pasta", IngredientNames.normalize("Spaghetti"));
        assertEquals("berry", IngredientNames.normalize("berries"));
    }

    @Test
    void dietaryConflictsAreNeverRecommended() {
        RecipeSpec chicken = new RecipeSpec(2, "Chicken Rice", DietType.GENERAL, Course.MAIN, 2,
                List.of(line("chicken breast", 300, Unit.G, Category.MEAT_SEAFOOD), line("rice", 200, Unit.G, Category.GRAINS)));
        RecipeSpec salad = new RecipeSpec(3, "Green Salad", DietType.VEGAN, Course.SALAD, 2,
                List.of(line("lettuce", 1, Unit.PCS, Category.VEGETABLES)));
        RecommendationContext vegan = new RecommendationContext(DietType.VEGAN, Set.of(), 3, TODAY);
        List<RecipeMatch> out = engine.recommend(List.of(chicken, salad, vegetablePasta()), pantry(), vegan, "max-match", null);
        assertEquals(List.of("Green Salad"), out.stream().map(m -> m.recipe().name()).toList());

        RecommendationContext veg = new RecommendationContext(DietType.VEGETARIAN, Set.of(), 3, TODAY);
        assertEquals(2, engine.recommend(List.of(chicken, salad, vegetablePasta()), pantry(), veg, null, null).size());
    }

    @Test
    void allergiesExcludeRecipes() {
        RecommendationContext dairyFree = new RecommendationContext(DietType.GENERAL, Set.of(Allergen.DAIRY), 3, TODAY);
        assertTrue(engine.recommend(List.of(vegetablePasta()), pantry(), dairyFree, null, null).isEmpty());
        assertFalse(Allergen.EGGS.isPresentIn("eggplant"));
        assertFalse(Allergen.DAIRY.isPresentIn("soy milk"));
        assertFalse(Allergen.GLUTEN.isPresentIn("goat cheese"));
    }

    @Test
    void substitutionsRespectDishAndDiet() {
        RecipeSpec cake = new RecipeSpec(4, "Cake", DietType.VEGETARIAN, Course.BAKING, 8,
                List.of(line("butter", 100, Unit.G, Category.DAIRY), line("lemon juice", 2, Unit.TBSP, Category.FRUITS),
                        line("egg", 2, Unit.PCS, Category.DAIRY), line("sugar", 200, Unit.G, Category.OTHERS)));
        RecipeSpec saute = new RecipeSpec(5, "Saute", DietType.VEGETARIAN, Course.MAIN, 2,
                List.of(line("butter", 40, Unit.G, Category.DAIRY)));

        List<Substitution> forCake = subs.findSubstitutes(cake.lines().get(0), 100, Unit.G, cake, pantry(), DietType.GENERAL, Set.of());
        assertTrue(forCake.stream().noneMatch(s -> s.replacement().equals("Olive oil")), "no olive oil in baking");
        List<Substitution> forSaute = subs.findSubstitutes(saute.lines().get(0), 40, Unit.G, saute, pantry(), DietType.GENERAL, Set.of());
        Substitution oil = forSaute.stream().filter(s -> s.replacement().equals("Olive oil")).findFirst().orElseThrow();
        assertEquals("30 g", oil.recommendedQuantity().format());

        List<Substitution> lemon = subs.findSubstitutes(cake.lines().get(1), 2, Unit.TBSP, cake, pantry(), DietType.GENERAL, Set.of());
        assertTrue(lemon.stream().noneMatch(s -> s.replacement().equals("Vinegar")), "no vinegar in desserts");

        List<Substitution> egg = subs.findSubstitutes(cake.lines().get(2), 2, Unit.PCS, cake, pantry(), DietType.GENERAL, Set.of());
        assertEquals("2 tbsp", egg.get(0).recommendedQuantity().format());

        List<Substitution> sugarVegan = subs.findSubstitutes(cake.lines().get(3), 200, Unit.G, cake, pantry(), DietType.VEGAN, Set.of());
        assertTrue(sugarVegan.stream().noneMatch(s -> s.replacement().equals("Honey")), "no honey for vegans");
    }

    @Test
    void substituteInPantryIsReported() {
        RecipeSpec saute = new RecipeSpec(5, "Saute", DietType.VEGETARIAN, Course.MAIN, 2,
                List.of(line("butter", 40, Unit.G, Category.DAIRY), line("onion", 1, Unit.PCS, Category.VEGETABLES)));
        PantrySnapshot p = pantry(stock(1, "olive oil", 500, Unit.ML, Category.OTHERS, 200),
                stock(2, "onion", 2, Unit.PCS, Category.VEGETABLES, 10));
        RecipeMatch m = engine.evaluate(saute, p, general, null);
        assertEquals(IngredientStatus.CAN_SUBSTITUTE, m.lines().get(0).status());
        assertTrue(m.lines().get(0).substitutions().get(0).inPantry());
        assertEquals(50, m.matchPercent());
    }

    @Test
    void expiringFirstStrategyPrefersRescues() {
        RecipeSpec spinachSoup = new RecipeSpec(6, "Spinach Soup", DietType.VEGAN, Course.SOUP, 2,
                List.of(line("spinach", 200, Unit.G, Category.VEGETABLES), line("onion", 1, Unit.PCS, Category.VEGETABLES)));
        RecipeSpec riceBowl = new RecipeSpec(7, "Rice Bowl", DietType.VEGAN, Course.MAIN, 2,
                List.of(line("rice", 150, Unit.G, Category.GRAINS), line("onion", 1, Unit.PCS, Category.VEGETABLES)));
        PantrySnapshot p = pantry(stock(1, "spinach", 150, Unit.G, Category.VEGETABLES, 1),
                stock(2, "rice", 1, Unit.KG, Category.GRAINS, 300),
                stock(3, "onion", 3, Unit.PCS, Category.VEGETABLES, 30));
        List<RecipeMatch> byMatch = engine.recommend(List.of(spinachSoup, riceBowl), p, general, "max-match", null);
        List<RecipeMatch> byExpiry = engine.recommend(List.of(spinachSoup, riceBowl), p, general, "expiring-first", null);
        assertEquals("Rice Bowl", byMatch.get(0).recipe().name());
        assertEquals("Spinach Soup", byExpiry.get(0).recipe().name());
    }

    @Test
    void wasteRiskConsidersUrgencyAndQuantity() {
        int expired = WasteRiskCalculator.score(stock(1, "milk", 1, Unit.L, Category.DAIRY, -2), TODAY);
        int bigTomorrow = WasteRiskCalculator.score(stock(2, "chicken", 1, Unit.KG, Category.MEAT_SEAFOOD, 1), TODAY);
        int smallTomorrow = WasteRiskCalculator.score(stock(3, "chicken", 100, Unit.G, Category.MEAT_SEAFOOD, 1), TODAY);
        int nextWeek = WasteRiskCalculator.score(stock(4, "carrot", 1, Unit.KG, Category.VEGETABLES, 7), TODAY);
        assertEquals(100, expired);
        assertTrue(bigTomorrow > smallTomorrow);
        assertTrue(smallTomorrow > nextWeek);
        assertEquals(AlertLevel.EXPIRES_TODAY, WasteRiskCalculator.level(TODAY, TODAY, 3));
        assertEquals(AlertLevel.EXPIRING_SOON, WasteRiskCalculator.level(TODAY.plusDays(3), TODAY, 3));
        assertEquals(AlertLevel.FRESH, WasteRiskCalculator.level(TODAY.plusDays(4), TODAY, 3));
    }

    @Test
    void shoppingListConsolidatesAndSubtractsPantry() {
        RecipeSpec a = new RecipeSpec(8, "A", DietType.VEGAN, Course.MAIN, 2,
                List.of(line("pasta", 300, Unit.G, Category.GRAINS), line("tomato", 2, Unit.PCS, Category.VEGETABLES)));
        RecipeSpec b = new RecipeSpec(9, "B", DietType.VEGAN, Course.MAIN, 2,
                List.of(line("Pasta", 0.2, Unit.KG, Category.GRAINS), line("olive oil", 1, Unit.TBSP, Category.OTHERS)));
        PantrySnapshot p = pantry(stock(1, "pasta", 200, Unit.G, Category.GRAINS, 100),
                stock(2, "tomatoes", 5, Unit.PCS, Category.VEGETABLES, 4));
        List<ShoppingCalculator.Need> list = ShoppingCalculator.calculate(
                List.of(new ShoppingCalculator.Planned(a, 2), new ShoppingCalculator.Planned(b, 4)), p);
        // pasta: 300 + 400 (b doubled) = 700 g needed - 200 g in pantry = 500 g
        ShoppingCalculator.Need pasta = list.stream().filter(n -> n.name().equals("Pasta")).findFirst().orElseThrow();
        assertEquals("500 g", pasta.toBuy().format());
        assertEquals(Set.of("A", "B"), pasta.forRecipes());
        assertTrue(list.stream().noneMatch(n -> n.name().equals("Tomato")), "tomatoes are covered");
        assertTrue(list.stream().anyMatch(n -> n.name().equals("Olive oil") && n.toBuy().format().equals("30 ml")));
    }

    @Test
    void cookingUsesEarliestExpiringStockFirst() {
        RecipeSpec r = new RecipeSpec(10, "Milkshake", DietType.VEGETARIAN, Course.DRINK, 1,
                List.of(line("milk", 300, Unit.ML, Category.DAIRY)));
        PantrySnapshot p = pantry(stock(1, "milk", 1, Unit.L, Category.DAIRY, 6),
                stock(2, "milk", 200, Unit.ML, Category.DAIRY, 1));
        List<ConsumptionPlanner.Deduction> d = ConsumptionPlanner.plan(r, 1, p);
        assertEquals(Long.valueOf(2), d.get(0).stockId());
        assertEquals(0, d.get(0).remaining(), 1e-9);
        assertEquals(Long.valueOf(1), d.get(1).stockId());
        assertEquals(0.9, d.get(1).remaining(), 1e-9);
    }
}
