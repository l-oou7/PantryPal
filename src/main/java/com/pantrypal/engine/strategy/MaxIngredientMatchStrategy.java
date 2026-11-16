package com.pantrypal.engine.strategy;

import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

/** Ranks purely by the quantity-weighted ingredient match percentage. */
public class MaxIngredientMatchStrategy implements RecommendationStrategy {

    public String key() { return "max-match"; }

    public String label() { return "Maximum Ingredient Match"; }

    public String description() { return "Recipes you can cover best with what you already have."; }

    public double score(RecipeMatch m, RecommendationContext ctx) {
        return m.matchPercent();
    }

    public String reason(RecipeMatch m, RecommendationContext ctx) {
        if (m.canCookNow()) return "You have everything you need!";
        return "You have " + m.fullyAvailable() + " of " + m.requiredCount() + " ingredients";
    }
}
