package com.pantrypal.domain;

import java.util.List;

/**
 * Common allergens with the ingredient keywords that indicate them.
 * Matching is done on normalised ingredient names (see IngredientNames).
 */
public enum Allergen {
    DAIRY("Dairy", List.of("milk", "cheese", "butter", "cream", "yogurt", "yoghurt", "parmesan", "mozzarella",
            "cheddar", "feta", "ghee", "buttermilk", "sour cream", "cream cheese", "halloumi", "paneer")),
    EGGS("Eggs", List.of("egg", "mayonnaise")),
    GLUTEN("Gluten", List.of("flour", "bread", "pasta", "spaghetti", "noodle", "couscous", "tortilla", "breadcrumb",
            "soy sauce", "oat", "barley", "pita", "bun", "cracker")),
    PEANUTS("Peanuts", List.of("peanut")),
    TREE_NUTS("Tree nuts", List.of("almond", "walnut", "cashew", "pecan", "pistachio", "hazelnut")),
    SHELLFISH("Shellfish", List.of("shrimp", "prawn", "crab", "lobster", "mussel", "clam", "scallop")),
    FISH("Fish", List.of("salmon", "tuna", "cod", "fish", "anchovy", "sardine", "tilapia")),
    SOY("Soy", List.of("soy", "tofu", "tempeh", "edamame", "miso", "tamari")),
    SESAME("Sesame", List.of("sesame", "tahini"));

    private final String label;
    private final List<String> keywords;

    Allergen(String label, List<String> keywords) {
        this.label = label;
        this.keywords = keywords;
    }

    public String getLabel() { return label; }
    public List<String> getKeywords() { return keywords; }

    /**
     * True when the (normalised) ingredient name contains one of this allergen's keywords.
     * Plant milks and vegan butter are not dairy; almond milk still counts as a tree nut.
     */
    public boolean isPresentIn(String normalisedName) {
        if (normalisedName == null) return false;
        if (this == DAIRY && IngredientKnowledge.isPlantBasedAlternative(normalisedName)) return false;
        if (this == GLUTEN && (normalisedName.contains("almond flour") || normalisedName.contains("coconut flour")
                || normalisedName.contains("rice flour") || normalisedName.contains("gluten free")
                || normalisedName.contains("zucchini noodle") || normalisedName.contains("tamari"))) return false;
        return TextMatch.containsAnyWord(normalisedName, keywords);
    }
}
