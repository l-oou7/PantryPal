package com.pantrypal.engine;

/** A concrete substitution suggestion for one recipe line. */
public record Substitution(String original, String replacement, Quantity recommendedQuantity,
                           String explanation, boolean inPantry) {
}
