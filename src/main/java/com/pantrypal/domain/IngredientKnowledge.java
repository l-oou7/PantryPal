package com.pantrypal.domain;

import java.util.List;

/**
 * Small rule base about ingredients, used for dietary checks.
 * Names passed in must already be normalised (lower case, singular).
 */
public final class IngredientKnowledge {

    private static final List<String> MEAT_SEAFOOD = List.of(
            "chicken", "beef", "pork", "lamb", "mutton", "turkey", "bacon", "ham", "sausage", "salami", "pepperoni",
            "steak", "mince", "ground beef", "duck", "veal", "anchovy", "gelatin",
            "salmon", "tuna", "cod", "fish", "shrimp", "prawn", "crab", "lobster", "mussel", "clam", "scallop",
            "sardine", "tilapia");

    private static final List<String> OTHER_ANIMAL_PRODUCTS = List.of(
            "egg", "honey", "milk", "cheese", "butter", "cream", "yogurt", "yoghurt", "ghee", "mayonnaise",
            "parmesan", "mozzarella", "cheddar", "feta", "halloumi", "paneer", "buttermilk");

    private static final List<String> PLANT_BASED_MARKERS = List.of(
            "soy milk", "oat milk", "almond milk", "coconut milk", "rice milk", "vegan", "plant",
            "coconut cream", "coconut yogurt", "peanut butter", "almond butter", "nutritional yeast", "butternut",
            "eggplant", "cream of tartar", "flax");

    private static final List<String> HIGH_CARB = List.of(
            "sugar", "flour", "pasta", "spaghetti", "rice", "bread", "potato", "noodle", "oat", "honey", "maple syrup",
            "tortilla", "couscous", "quinoa", "banana", "corn", "lentil", "chickpea", "bean", "pita", "cracker");

    private IngredientKnowledge() {}

    public static boolean isPlantBasedAlternative(String name) {
        return PLANT_BASED_MARKERS.stream().anyMatch(name::contains);
    }

    public static boolean isMeatOrSeafood(String name, Category category) {
        if (isPlantBasedAlternative(name)) return false;
        if (category == Category.MEAT_SEAFOOD) return true;
        return TextMatch.containsAnyWord(name, MEAT_SEAFOOD);
    }

    public static boolean isAnimalProduct(String name, Category category) {
        if (isPlantBasedAlternative(name)) return false;
        if (isMeatOrSeafood(name, category)) return true;
        if (category == Category.DAIRY) return true;
        return TextMatch.containsAnyWord(name, OTHER_ANIMAL_PRODUCTS);
    }

    public static boolean isHighCarb(String name) {
        if (name.contains("cauliflower rice") || name.contains("almond flour") || name.contains("coconut flour")
                || name.contains("zucchini noodle") || name.contains("sugar free")) return false;
        return TextMatch.containsAnyWord(name, HIGH_CARB);
    }

    /** Whether a single ingredient is acceptable for someone following the given diet. */
    public static boolean suits(DietType diet, String name, Category category) {
        return switch (diet) {
            case GENERAL -> true;
            case VEGETARIAN -> !isMeatOrSeafood(name, category);
            case VEGAN -> !isAnimalProduct(name, category);
            case KETO -> !isHighCarb(name);
        };
    }
}
