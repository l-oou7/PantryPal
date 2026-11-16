package com.pantrypal.engine.strategy;

import com.pantrypal.domain.DietType;
import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

/**
 * Favours recipes written specifically for the user's diet (a vegan dish for a vegan, rather than
 * a general dish that happens to be compatible). Conflicting recipes are already filtered out
 * by the engine for every strategy.
 * score = match% + 30 if the recipe's own category equals the user's diet
 */
public class DietaryPreferenceStrategy implements RecommendationStrategy {

    public String key() { return "dietary"; }

    public String label() { return "Dietary Preference"; }

    public String description() { return "Dishes made for your diet come first."; }

    public double score(RecipeMatch m, RecommendationContext ctx) {
        return m.matchPercent() + (isTailored(m, ctx) ? 30 : 0);
    }

    public String reason(RecipeMatch m, RecommendationContext ctx) {
        if (isTailored(m, ctx)) return "A " + ctx.diet().getLabel().toLowerCase() + " favourite";
        return "Fits your " + ctx.diet().getLabel().toLowerCase() + " preference";
    }

    private boolean isTailored(RecipeMatch m, RecommendationContext ctx) {
        DietType d = ctx.diet();
        return d != DietType.GENERAL && m.recipe().dietaryCategory() == d;
    }
}
