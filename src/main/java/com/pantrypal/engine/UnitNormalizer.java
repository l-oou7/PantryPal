package com.pantrypal.engine;

import com.pantrypal.domain.Dimension;
import com.pantrypal.domain.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Automatic unit normalisation. Every quantity is converted to the base unit of its
 * dimension (g, ml, pcs) before quantities are compared, added or subtracted.
 */
public final class UnitNormalizer {

    private UnitNormalizer() {}

    public static Unit baseUnit(Dimension d) {
        return switch (d) {
            case MASS -> Unit.G;
            case VOLUME -> Unit.ML;
            case COUNT -> Unit.PCS;
        };
    }

    public static double toBase(double amount, Unit unit) {
        return amount * unit.getToBase();
    }

    public static boolean compatible(Unit a, Unit b) {
        return a != null && b != null && a.getDimension() == b.getDimension();
    }

    /** Converts between two units of the same dimension, e.g. 1.5 kg -> 1500 g. */
    public static double convert(double amount, Unit from, Unit to) {
        if (!compatible(from, to)) {
            throw new IllegalArgumentException("Cannot convert " + from.getSymbol() + " to " + to.getSymbol());
        }
        return amount * from.getToBase() / to.getToBase();
    }

    /** Picks a readable unit: kg above 1000 g, l above 1000 ml. Kitchen spoons stay as ml below that. */
    public static Quantity humanize(double baseAmount, Dimension d) {
        return switch (d) {
            case MASS -> baseAmount >= 1000 ? new Quantity(round(baseAmount / 1000, 2), Unit.KG)
                                            : new Quantity(round(baseAmount, 0), Unit.G);
            case VOLUME -> baseAmount >= 1000 ? new Quantity(round(baseAmount / 1000, 2), Unit.L)
                                              : new Quantity(round(baseAmount, 0), Unit.ML);
            case COUNT -> new Quantity(Math.ceil(baseAmount - 1e-9), Unit.PCS);
        };
    }

    public static double round(double v, int places) {
        return BigDecimal.valueOf(v).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public static String format(Quantity q) {
        double a = q.amount();
        String num;
        if (Math.abs(a - Math.rint(a)) < 1e-9) num = String.valueOf((long) Math.rint(a));
        else num = BigDecimal.valueOf(round(a, 2)).stripTrailingZeros().toPlainString();
        return num + " " + q.unit().getSymbol();
    }
}
