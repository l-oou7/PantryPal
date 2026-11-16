package com.pantrypal.engine;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Course;
import com.pantrypal.domain.Unit;

import java.util.Set;

/**
 * A swap rule: replace {@code original} with {@code replacement}.
 * <ul>
 *   <li>If {@code fromUnit}/{@code toUnit} are null, the replacement uses the original's unit and amount x ratio.</li>
 *   <li>Otherwise the original amount is first converted to {@code fromUnit}, multiplied by the ratio,
 *       and expressed in {@code toUnit} (e.g. 1 egg -> 1 tbsp ground flaxseed + 3 tbsp water).</li>
 *   <li>{@code onlyCourses} / {@code excludedCourses} keep swaps out of dishes they would ruin
 *       (vinegar instead of lemon juice in a dessert, olive oil instead of butter in baking).</li>
 * </ul>
 */
public record SubstitutionRule(String original, String replacement, Category replacementCategory, double ratio,
                               Unit fromUnit, Unit toUnit, String note,
                               Set<Course> onlyCourses, Set<Course> excludedCourses) {

    public SubstitutionRule {
        original = IngredientNames.normalize(original);
        replacement = IngredientNames.normalize(replacement);
        onlyCourses = onlyCourses == null ? Set.of() : onlyCourses;
        excludedCourses = excludedCourses == null ? Set.of() : excludedCourses;
    }

    public boolean suitsCourse(Course course) {
        if (course == null) return true;
        if (excludedCourses.contains(course)) return false;
        return onlyCourses.isEmpty() || onlyCourses.contains(course);
    }

    /** Recommended replacement amount for {@code amount} of the original in {@code unit}. */
    public Quantity apply(double amount, Unit unit) {
        if (fromUnit == null || toUnit == null) {
            return new Quantity(amount * ratio, unit);
        }
        double inFrom = UnitNormalizer.compatible(unit, fromUnit) ? UnitNormalizer.convert(amount, unit, fromUnit) : amount;
        return new Quantity(inFrom * ratio, toUnit);
    }
}
