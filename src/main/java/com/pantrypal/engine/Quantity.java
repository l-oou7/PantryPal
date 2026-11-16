package com.pantrypal.engine;

import com.pantrypal.domain.Dimension;
import com.pantrypal.domain.Unit;

/** An amount with a unit. Immutable. */
public record Quantity(double amount, Unit unit) {

    public Quantity {
        if (unit == null) throw new IllegalArgumentException("unit is required");
        if (Double.isNaN(amount) || amount < 0) throw new IllegalArgumentException("amount must be >= 0");
    }

    public static Quantity ofBase(double baseAmount, Dimension dimension) {
        return new Quantity(baseAmount, UnitNormalizer.baseUnit(dimension));
    }

    public double toBase() { return UnitNormalizer.toBase(amount, unit); }

    public Dimension dimension() { return unit.getDimension(); }

    public Quantity scale(double factor) { return new Quantity(amount * factor, unit); }

    /** Expresses the amount in the most readable unit of its dimension (1500 g -> 1.5 kg). */
    public Quantity humanize() { return UnitNormalizer.humanize(toBase(), dimension()); }

    public String format() { return UnitNormalizer.format(this); }

    @Override
    public String toString() { return format(); }
}
