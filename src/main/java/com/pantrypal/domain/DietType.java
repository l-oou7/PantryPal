package com.pantrypal.domain;

/** Dietary categories used both for recipes and for user preferences. */
public enum DietType {
    GENERAL("General"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    KETO("Keto");

    private final String label;

    DietType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
