package com.pantrypal.engine;

import com.pantrypal.domain.*;

import java.util.*;

/**
 * Pure Fabrication (GRASP): knows how ingredients can be swapped. Suggestions are filtered so that
 * a replacement never breaks the user's diet, never contains one of their allergens and suits the dish.
 */
public class SubstitutionEngine {

    private final Map<String, List<SubstitutionRule>> rules = new HashMap<>();

    public SubstitutionEngine() {
        initializeCommonSubstitutions();
    }

    public void addRule(SubstitutionRule rule) {
        rules.computeIfAbsent(rule.original(), k -> new ArrayList<>()).add(rule);
    }

    public List<SubstitutionRule> rulesFor(String ingredient) {
        return rules.getOrDefault(IngredientNames.normalize(ingredient), List.of());
    }

    /**
     * All suitable substitutes for {@code amount} of a recipe line, best first
     * (replacements already in the pantry come first, then in rule order).
     */
    public List<Substitution> findSubstitutes(RecipeLine line, double amount, Unit unit, RecipeSpec recipe,
                                              PantrySnapshot pantry, DietType diet, Set<Allergen> allergies) {
        List<Substitution> out = new ArrayList<>();
        for (SubstitutionRule r : rulesFor(line.name())) {
            if (!r.suitsCourse(recipe.course())) continue;
            if (diet != null && !IngredientKnowledge.suits(diet, r.replacement(), r.replacementCategory())) continue;
            if (allergies != null && allergies.stream().anyMatch(a -> a.isPresentIn(r.replacement()))) continue;
            Quantity q = r.apply(amount, unit);
            boolean inPantry = pantry != null && pantry.has(r.replacement());
            out.add(new Substitution(IngredientNames.display(line.name()), IngredientNames.display(r.replacement()),
                    roundForDisplay(q), r.note(), inPantry));
        }
        out.sort(Comparator.comparing((Substitution s) -> !s.inPantry()));
        return out;
    }

    private static Quantity roundForDisplay(Quantity q) {
        double a = q.amount();
        double r = a >= 10 ? Math.round(a) : UnitNormalizer.round(a, 2);
        return new Quantity(r, q.unit());
    }

    private void add(String from, String to, Category cat, double ratio, String note) {
        addRule(new SubstitutionRule(from, to, cat, ratio, null, null, note, null, null));
    }

    private void add(String from, String to, Category cat, double ratio, String note,
                     Set<Course> only, Set<Course> excluded) {
        addRule(new SubstitutionRule(from, to, cat, ratio, null, null, note, only, excluded));
    }

    private void addConverted(String from, String to, Category cat, double ratio, Unit fromUnit, Unit toUnit,
                              String note, Set<Course> only, Set<Course> excluded) {
        addRule(new SubstitutionRule(from, to, cat, ratio, fromUnit, toUnit, note, only, excluded));
    }

    private void initializeCommonSubstitutions() {
        Set<Course> baked = EnumSet.of(Course.BAKING, Course.DESSERT);
        Set<Course> sweet = EnumSet.of(Course.BAKING, Course.DESSERT, Course.BREAKFAST, Course.DRINK);

        // Fats
        add("butter", "olive oil", Category.OTHERS, 0.75,
                "Use about three quarters of the amount. Oil has no water content, so less is needed. Great for savoury cooking.",
                null, baked);
        add("butter", "coconut oil", Category.OTHERS, 1.0,
                "Swap 1:1. Solid coconut oil behaves like butter in baking; it adds a light coconut note.");
        add("butter", "vegan butter", Category.OTHERS, 1.0, "Plant-based butter swaps 1:1 in any recipe.");
        add("olive oil", "vegetable oil", Category.OTHERS, 1.0, "Neutral oil swaps 1:1; you lose a little fruity flavour.");
        add("vegetable oil", "olive oil", Category.OTHERS, 1.0, "Swap 1:1 for frying and roasting.");

        // Dairy
        add("milk", "soy milk", Category.DAIRY, 1.0, "Unsweetened soy milk swaps 1:1 and has similar protein, so it thickens sauces well.");
        add("milk", "oat milk", Category.DAIRY, 1.0, "Swap 1:1. Slightly sweeter, great in pancakes and porridge.");
        add("milk", "almond milk", Category.DAIRY, 1.0, "Swap 1:1. Thinner than dairy milk, so sauces may need a little longer to reduce.");
        add("heavy cream", "coconut cream", Category.CANNED_GOODS, 1.0, "Full-fat coconut cream swaps 1:1 and stays rich.");
        add("cream", "coconut cream", Category.CANNED_GOODS, 1.0, "Full-fat coconut cream swaps 1:1 and stays rich.");
        add("sour cream", "greek yogurt", Category.DAIRY, 1.0, "Same tang and thickness. Add at the end so it doesn't split.");
        add("yogurt", "greek yogurt", Category.DAIRY, 1.0, "Thicker; loosen with a splash of water if needed.");
        add("greek yogurt", "yogurt", Category.DAIRY, 1.0, "Thinner; strain it for a few minutes for a thicker result.");
        add("yogurt", "coconut yogurt", Category.DAIRY, 1.0, "Dairy-free swap 1:1.");
        add("mayonnaise", "greek yogurt", Category.DAIRY, 1.0, "Lighter and tangier; season with a pinch of salt and lemon.");
        add("parmesan", "nutritional yeast", Category.SPICES, 0.5,
                "Use half as much for a cheesy, savoury flavour without dairy.", null, baked);
        add("cheddar", "mozzarella", Category.DAIRY, 1.0, "Melts just as well, with a milder taste.");
        add("mozzarella", "cheddar", Category.DAIRY, 1.0, "Melts well and adds a sharper taste.");
        add("buttermilk", "milk", Category.DAIRY, 1.0, "Stir 1 tbsp lemon juice or vinegar into each cup of milk and wait 5 minutes.");

        // Eggs
        addConverted("egg", "ground flaxseed", Category.GRAINS, 1.0, Unit.PCS, Unit.TBSP,
                "For each egg mix 1 tbsp ground flaxseed with 3 tbsp water and rest 5 minutes. Binds well in baking.",
                sweet, null);
        addConverted("egg", "banana", Category.FRUITS, 0.5, Unit.PCS, Unit.PCS,
                "Half a mashed ripe banana per egg. Adds moisture and sweetness, so only for sweet dishes.",
                sweet, null);

        // Acids
        add("lemon juice", "lime juice", Category.FRUITS, 1.0, "Swap 1:1; a slightly more floral sourness.");
        add("lemon juice", "vinegar", Category.OTHERS, 0.5,
                "Vinegar is sharper, so use half the amount. Works in dressings and savoury dishes, not in desserts.",
                null, baked);
        add("lime juice", "lemon juice", Category.FRUITS, 1.0, "Swap 1:1.");
        add("vinegar", "lemon juice", Category.FRUITS, 2.0, "Lemon juice is milder, so use about twice as much.");

        // Sweeteners
        add("sugar", "honey", Category.OTHERS, 0.75,
                "Honey is sweeter: use three quarters of the amount and reduce other liquids slightly.");
        add("sugar", "maple syrup", Category.OTHERS, 0.75, "Use three quarters of the amount and reduce other liquids slightly.");
        add("honey", "maple syrup", Category.OTHERS, 1.0, "Swap 1:1; vegan-friendly.");
        add("maple syrup", "honey", Category.OTHERS, 1.0, "Swap 1:1.");

        // Starches & grains
        add("rice", "quinoa", Category.GRAINS, 1.0, "Cook the same weight of quinoa; it's ready in about 15 minutes.");
        add("rice", "cauliflower rice", Category.VEGETABLES, 1.5, "Low-carb swap: use a bit more and cook only 5 minutes.");
        add("pasta", "zucchini noodle", Category.VEGETABLES, 1.5,
                "Low-carb swap: use about 1.5x the weight and cook for just 2 minutes.", null, baked);
        add("pasta", "rice", Category.GRAINS, 1.0, "Swap the same weight; works for most sauces.", null, baked);
        add("flour", "almond flour", Category.GRAINS, 1.0,
                "Gluten-free and low-carb. Results are denser; add an extra egg or binder in cakes.", null, null);
        add("bread crumb", "rolled oat", Category.GRAINS, 1.0, "Pulse oats briefly; they crisp up just like crumbs.");
        add("cornstarch", "flour", Category.GRAINS, 2.0, "Flour thickens half as well, so use twice the amount.");
        add("potato", "sweet potato", Category.VEGETABLES, 1.0, "Swap 1:1; slightly sweeter and cooks a bit faster.");

        // Proteins
        add("chicken breast", "tofu", Category.OTHERS, 1.0, "Press firm tofu for 10 minutes, then cube and cook like chicken.",
                null, baked);
        add("chicken breast", "chickpea", Category.CANNED_GOODS, 0.8,
                "Use slightly less weight of drained chickpeas; add them near the end.", EnumSet.of(Course.MAIN, Course.SALAD, Course.SOUP), null);
        add("ground beef", "lentil", Category.GRAINS, 0.8, "Cooked lentils give the same texture in sauces; use a little less weight.");
        add("ground beef", "ground turkey", Category.MEAT_SEAFOOD, 1.0, "Leaner; add a splash of oil when browning.");
        add("chicken broth", "vegetable broth", Category.CANNED_GOODS, 1.0, "Swap 1:1.");
        add("beef broth", "vegetable broth", Category.CANNED_GOODS, 1.0, "Swap 1:1; add a dash of soy sauce for depth.");
        add("shrimp", "chicken breast", Category.MEAT_SEAFOOD, 1.0, "Cut into bite-size pieces; cook until no longer pink.",
                EnumSet.of(Course.MAIN, Course.SALAD), null);

        // Vegetables & aromatics
        addConverted("garlic", "garlic powder", Category.SPICES, 0.125, Unit.PCS, Unit.TSP,
                "1/8 tsp garlic powder replaces one clove. Add it with the other spices.", null, null);
        add("onion", "shallot", Category.VEGETABLES, 2.0, "Shallots are smaller and milder: use two for each onion.");
        add("onion", "spring onion", Category.VEGETABLES, 3.0, "Use about three spring onions per onion; add late for freshness.");
        add("spinach", "kale", Category.VEGETABLES, 1.0, "Remove the stems and cook a few minutes longer.");
        add("tomato", "canned tomato", Category.CANNED_GOODS, 1.0, "Same amount of chopped canned tomatoes; drain for salads.",
                null, EnumSet.of(Course.SALAD));
        add("canned tomato", "tomato", Category.VEGETABLES, 1.0, "Chop fresh tomatoes and simmer 5 minutes longer.");
        add("bell pepper", "zucchini", Category.VEGETABLES, 1.0, "Similar crunch and cooking time.");
        add("mushroom", "zucchini", Category.VEGETABLES, 1.0, "Holds texture well in sauces and stir-fries.");
        add("soy sauce", "tamari", Category.SPICES, 1.0, "Gluten-free swap 1:1.");
        add("cilantro", "parsley", Category.SPICES, 1.0, "Swap 1:1; milder, less citrusy.");
        add("basil", "spinach", Category.VEGETABLES, 1.0, "Adds green freshness, but not basil's aroma. Fine in pesto with extra garlic.",
                null, baked);
    }
}
