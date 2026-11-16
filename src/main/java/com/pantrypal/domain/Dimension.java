package com.pantrypal.domain;

/** Physical dimension of a unit. Only quantities of the same dimension can be compared. */
public enum Dimension {
    MASS("g"), VOLUME("ml"), COUNT("pcs");

    private final String baseSymbol;

    Dimension(String baseSymbol) { this.baseSymbol = baseSymbol; }

    public String getBaseSymbol() { return baseSymbol; }
}
