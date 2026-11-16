package com.pantrypal.domain;

/**
 * Measurement units with their conversion factor to the base unit of their dimension
 * (grams for mass, millilitres for volume, pieces for count).
 */
public enum Unit {
    MG("mg", Dimension.MASS, 0.001),
    G("g", Dimension.MASS, 1),
    KG("kg", Dimension.MASS, 1000),
    OZ("oz", Dimension.MASS, 28.3495),
    LB("lb", Dimension.MASS, 453.592),
    ML("ml", Dimension.VOLUME, 1),
    L("l", Dimension.VOLUME, 1000),
    TSP("tsp", Dimension.VOLUME, 5),
    TBSP("tbsp", Dimension.VOLUME, 15),
    CUP("cup", Dimension.VOLUME, 240),
    PCS("pcs", Dimension.COUNT, 1);

    private final String symbol;
    private final Dimension dimension;
    private final double toBase;

    Unit(String symbol, Dimension dimension, double toBase) {
        this.symbol = symbol;
        this.dimension = dimension;
        this.toBase = toBase;
    }

    public String getSymbol() { return symbol; }
    public Dimension getDimension() { return dimension; }
    public double getToBase() { return toBase; }

    /** Parses "kg", "KG", "Kilograms" etc. Returns null when unknown. */
    public static Unit parse(String text) {
        if (text == null) return null;
        String t = text.trim().toLowerCase();
        for (Unit u : values()) {
            if (u.symbol.equals(t) || u.name().equalsIgnoreCase(t)) return u;
        }
        return switch (t) {
            case "gram", "grams", "gr" -> G;
            case "kilogram", "kilograms", "kgs" -> KG;
            case "milligram", "milligrams" -> MG;
            case "litre", "liter", "litres", "liters", "ltr" -> L;
            case "millilitre", "milliliter", "millilitres", "milliliters" -> ML;
            case "teaspoon", "teaspoons" -> TSP;
            case "tablespoon", "tablespoons", "tbs" -> TBSP;
            case "cups" -> CUP;
            case "piece", "pieces", "pc", "unit", "units", "each" -> PCS;
            case "ounce", "ounces" -> OZ;
            case "pound", "pounds", "lbs" -> LB;
            default -> null;
        };
    }
}
