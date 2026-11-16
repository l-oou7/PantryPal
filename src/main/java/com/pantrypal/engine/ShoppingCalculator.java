package com.pantrypal.engine;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Dimension;

import java.util.*;

/**
 * Turns planned recipes into a shopping list:
 * <ol>
 *   <li>collect every required ingredient of every planned recipe, scaled to its planned servings;</li>
 *   <li>consolidate duplicates (same normalised name and dimension) in base units;</li>
 *   <li>subtract usable (non-expired) pantry stock;</li>
 *   <li>keep only positive remainders, expressed in a readable unit.</li>
 * </ol>
 */
public final class ShoppingCalculator {

    /** A recipe that is planned, with the servings it is planned for. */
    public record Planned(RecipeSpec recipe, int servings) {}

    /** One line of the generated list. */
    public record Need(String name, Category category, Quantity required, Quantity inPantry, Quantity toBuy,
                       Set<String> forRecipes) {}

    private ShoppingCalculator() {}

    public static List<Need> calculate(List<Planned> plan, PantrySnapshot pantry) {
        record Key(String name, Dimension dim) {}
        Map<Key, double[]> totals = new LinkedHashMap<>();
        Map<Key, Category> categories = new HashMap<>();
        Map<Key, Set<String>> recipes = new HashMap<>();

        for (Planned p : plan) {
            double factor = (double) Math.max(1, p.servings()) / Math.max(1, p.recipe().servings());
            for (RecipeLine l : p.recipe().requiredLines()) {
                Key k = new Key(l.key(), l.unit().getDimension());
                totals.computeIfAbsent(k, x -> new double[1])[0] += l.quantity().scale(factor).toBase();
                categories.putIfAbsent(k, l.category());
                recipes.computeIfAbsent(k, x -> new TreeSet<>()).add(p.recipe().name());
            }
        }

        List<Need> out = new ArrayList<>();
        for (Map.Entry<Key, double[]> e : totals.entrySet()) {
            Key k = e.getKey();
            double need = e.getValue()[0];
            if (pantry.hasOnlyIncompatibleUnits(k.name(), k.dim())) continue; // stocked; can't compare -> assume covered
            double have = pantry.availableBase(k.name(), k.dim());
            double buy = need - have;
            if (buy <= 1e-9) continue;
            out.add(new Need(IngredientNames.display(k.name()), categories.get(k),
                    UnitNormalizer.humanize(need, k.dim()), UnitNormalizer.humanize(have, k.dim()),
                    UnitNormalizer.humanize(buy, k.dim()), recipes.get(k)));
        }
        out.sort(Comparator.comparing((Need n) -> n.category().ordinal()).thenComparing(Need::name));
        return out;
    }
}
