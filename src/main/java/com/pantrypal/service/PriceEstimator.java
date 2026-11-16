package com.pantrypal.service;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Dimension;
import com.pantrypal.domain.Unit;
import com.pantrypal.engine.UnitNormalizer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rough grocery prices per category, used to estimate the value of food used or wasted.
 * Prices are averages per kg / litre / piece and only need to be roughly right:
 * they turn "you used 14 items" into an understandable money figure on the dashboard.
 */
@Component
public class PriceEstimator {

    /** price per kilogram (also used per litre) */
    private final Map<Category, Double> perKg = new EnumMap<>(Category.class);
    /** price per piece */
    private final Map<Category, Double> perPiece = new EnumMap<>(Category.class);

    public PriceEstimator() {
        perKg.put(Category.FRUITS, 12.0);       perPiece.put(Category.FRUITS, 1.5);
        perKg.put(Category.VEGETABLES, 8.0);    perPiece.put(Category.VEGETABLES, 1.2);
        perKg.put(Category.DAIRY, 14.0);        perPiece.put(Category.DAIRY, 0.9);
        perKg.put(Category.MEAT_SEAFOOD, 42.0); perPiece.put(Category.MEAT_SEAFOOD, 9.0);
        perKg.put(Category.GRAINS, 8.0);        perPiece.put(Category.GRAINS, 3.0);
        perKg.put(Category.SPICES, 60.0);       perPiece.put(Category.SPICES, 2.0);
        perKg.put(Category.CANNED_GOODS, 12.0); perPiece.put(Category.CANNED_GOODS, 5.0);
        perKg.put(Category.OTHERS, 18.0);       perPiece.put(Category.OTHERS, 3.0);
    }

    public double estimate(Category category, double quantity, Unit unit) {
        Category c = category == null ? Category.OTHERS : category;
        double base = UnitNormalizer.toBase(quantity, unit);
        double value = unit.getDimension() == Dimension.COUNT ? base * perPiece.get(c) : base / 1000.0 * perKg.get(c);
        return UnitNormalizer.round(value, 2);
    }
}
