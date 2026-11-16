package com.pantrypal.engine;

import com.pantrypal.domain.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates how well a pantry covers a recipe.
 *
 * <h3>Match percentage</h3>
 * Every required (non-optional) ingredient is worth one point:
 * <pre>
 *   credit = 1                               if available >= required
 *          = available / required            if some, but not enough, is available
 *          = 0                               if it is missing (even if a substitute exists)
 *   match% = round(100 x sum(credit) / number of required ingredients)
 * </pre>
 * Quantities are compared after unit normalisation (kg->g, l->ml ...) and scaled to the requested servings.
 * Expired stock never counts. If the pantry stores the ingredient in an incomparable unit
 * (e.g. 3 pcs of garlic vs 10 g), the ingredient counts as available and is flagged.
 * Optional ingredients are ignored.
 * <p>
 * Example: Vegetable Pasta needs pasta, tomato, garlic, olive oil and cheese. With the first four in
 * sufficient quantity: 4 / 5 = 80%. With only half the pasta: (0.5 + 3) / 5 = 70%.
 */
public class MatchCalculator {

    private final SubstitutionEngine substitutions;

    public MatchCalculator(SubstitutionEngine substitutions) {
        this.substitutions = substitutions;
    }

    public RecipeMatch evaluate(RecipeSpec recipe, PantrySnapshot pantry, RecommendationContext ctx, Integer servings) {
        int target = servings == null || servings <= 0 ? recipe.servings() : servings;
        double factor = (double) target / Math.max(1, recipe.servings());

        List<LineAssessment> out = new ArrayList<>();
        double credit = 0;
        int required = 0, full = 0, partial = 0, missing = 0, subs = 0, expiringUsed = 0;
        int rescueSum = 0;

        for (RecipeLine line : recipe.lines()) {
            Quantity need = new Quantity(line.amount() * factor, line.unit());
            Dimension dim = line.unit().getDimension();
            int risk = pantry.maxWasteRisk(line.name());
            long days = pantry.daysUntilEarliestExpiry(line.name());

            LineAssessment la;
            if (!pantry.has(line.name())) {
                List<Substitution> options = substitutions.findSubstitutes(line, need.amount(), need.unit(), recipe,
                        pantry, ctx.diet(), ctx.allergies());
                IngredientStatus st;
                if (line.optional()) st = IngredientStatus.OPTIONAL;
                else if (options.stream().anyMatch(Substitution::inPantry)) st = IngredientStatus.CAN_SUBSTITUTE;
                else st = IngredientStatus.MISSING;
                la = new LineAssessment(line, need, null, need, st, 0, false, 0, Long.MAX_VALUE, options);
            } else if (pantry.hasOnlyIncompatibleUnits(line.name(), dim)) {
                la = new LineAssessment(line, need, null, null, IngredientStatus.HAVE_ENOUGH, 1, true, risk, days, List.of());
            } else {
                double haveBase = pantry.availableBase(line.name(), dim);
                double needBase = need.toBase();
                Quantity have = UnitNormalizer.humanize(haveBase, dim);
                if (haveBase + 1e-9 >= needBase) {
                    la = new LineAssessment(line, need, have, null, IngredientStatus.HAVE_ENOUGH, 1, false, risk, days, List.of());
                } else {
                    double c = needBase <= 0 ? 1 : haveBase / needBase;
                    Quantity shortBase = Quantity.ofBase(needBase - haveBase, dim);
                    Quantity shortInLineUnit = new Quantity(UnitNormalizer.convert(shortBase.amount(), shortBase.unit(), line.unit()), line.unit());
                    List<Substitution> options = substitutions.findSubstitutes(line, shortInLineUnit.amount(), line.unit(),
                            recipe, pantry, ctx.diet(), ctx.allergies());
                    la = new LineAssessment(line, need, have, shortBase.humanize(), IngredientStatus.NEED_MORE, c, false,
                            risk, days, options);
                }
            }
            out.add(la);

            if (line.optional()) continue;
            required++;
            credit += la.credit();
            switch (la.status()) {
                case HAVE_ENOUGH -> full++;
                case NEED_MORE -> partial++;
                case CAN_SUBSTITUTE -> { missing++; subs++; }
                case MISSING -> missing++;
                default -> { }
            }
            if (la.credit() > 0 && days <= ctx.expiringThresholdDays() + 4) {
                rescueSum += risk;
                if (days <= ctx.expiringThresholdDays()) expiringUsed++;
            }
        }

        int pct = required == 0 ? 100 : (int) Math.round(100.0 * credit / required);
        int rescue = Math.min(100, rescueSum);
        return new RecipeMatch(recipe, target, out, pct, full, partial, missing, subs, required, rescue, expiringUsed);
    }
}
