package com.pantrypal.domain;

/** Pantry / ingredient categories. */
public enum Category {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    DAIRY("Dairy"),
    MEAT_SEAFOOD("Meat & Seafood"),
    GRAINS("Grains"),
    SPICES("Spices"),
    CANNED_GOODS("Canned Goods"),
    OTHERS("Others");

    private final String label;

    Category(String label) { this.label = label; }

    public String getLabel() { return label; }
}
