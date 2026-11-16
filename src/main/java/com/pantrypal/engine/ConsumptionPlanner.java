package com.pantrypal.engine;

import com.pantrypal.domain.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out which pantry rows to reduce when a recipe is cooked. Uses the earliest-expiring
 * stock first (FIFO), so cooking always rescues the food most at risk.
 */
public final class ConsumptionPlanner {

    /** Reduce stock row {@code stockId} by {@code amount} (in that row's own unit). */
    public record Deduction(Long stockId, String name, double amount, double remaining, long daysUntilExpiry) {}

    private ConsumptionPlanner() {}

    public static List<Deduction> plan(RecipeSpec recipe, int servings, PantrySnapshot pantry) {
        double factor = (double) Math.max(1, servings) / Math.max(1, recipe.servings());
        List<Deduction> out = new ArrayList<>();
        for (RecipeLine line : recipe.lines()) {
            Dimension dim = line.unit().getDimension();
            double needBase = line.quantity().scale(factor).toBase();
            for (StockItem s : pantry.items(line.name())) {
                if (needBase <= 1e-9) break;
                if (s.unit().getDimension() != dim) continue;
                double takeBase = Math.min(needBase, s.baseQuantity());
                double takeInUnit = takeBase / s.unit().getToBase();
                double remaining = Math.max(0, s.quantity() - takeInUnit);
                if (remaining < 1e-6) remaining = 0;
                out.add(new Deduction(s.id(), s.name(), takeInUnit, remaining,
                        WasteRiskCalculator.daysLeft(s.expirationDate(), pantry.today())));
                needBase -= takeBase;
            }
        }
        return out;
    }
}
