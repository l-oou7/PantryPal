package com.pantrypal.engine.strategy;

import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

/**
 * Fewest things to buy: ranks by missing ingredients, then by ingredients you have too little of.
 * score = 100 - 25 x missing - 10 x partial  (+ a little for substitutes you have on hand)
 */
public class MinimumMissingIngredientsStrategy implements RecommendationStrategy {

    public String key() { return "min-missing"; }

    public String label() { return "Minimum Missing Ingredients"; }

    public String description() { return "The shortest shopping trip, or none at all."; }

    public double score(RecipeMatch m, RecommendationContext ctx) {
        return 100 - 25 * m.missing() - 10 * m.partial() + 8 * m.substitutable();
    }

    public String reason(RecipeMatch m, RecommendationContext ctx) {
        int buy = m.missing() - m.substitutable() + m.partial();
        if (buy <= 0) return m.substitutable() > 0 ? "No shopping needed with a quick swap" : "No shopping needed";
        return "Only " + buy + " item" + (buy == 1 ? "" : "s") + " to pick up";
    }
}
