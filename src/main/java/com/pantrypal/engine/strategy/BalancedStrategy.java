package com.pantrypal.engine.strategy;

import com.pantrypal.domain.DietType;
import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

/**
 * Default "Smart Mix": combines all recommendation priorities.
 * <pre>
 * score = 0.45 x match%                      (available ingredients and quantities)
 *       + 0.30 x rescueScore                 (expiring ingredients / waste reduction)
 *       + 0.15 x max(0, 100 - 30 x toBuy)    (missing ingredients)
 *       + 0.10 x dietBonus                   (100 if made for the user's diet)
 * </pre>
 */
public class BalancedStrategy implements RecommendationStrategy {

    public String key() { return "balanced"; }

    public String label() { return "Smart Mix"; }

    public String description() { return "Balances what you have, what's expiring, and what you'd need to buy."; }

    public double score(RecipeMatch m, RecommendationContext ctx) {
        int toBuy = m.missing() - m.substitutable();
        double missingPart = Math.max(0, 100 - 30 * toBuy);
        double dietBonus = ctx.diet() != DietType.GENERAL && m.recipe().dietaryCategory() == ctx.diet() ? 100 : 0;
        return 0.45 * m.matchPercent() + 0.30 * m.rescueScore() + 0.15 * missingPart + 0.10 * dietBonus;
    }

    public String reason(RecipeMatch m, RecommendationContext ctx) {
        if (m.canCookNow() && m.expiringIngredientsUsed() > 0) return "Ready to cook and saves expiring food";
        if (m.canCookNow()) return "Ready to cook right now";
        if (m.expiringIngredientsUsed() > 0) return "Uses " + m.expiringIngredientsUsed() + " ingredient(s) expiring soon";
        return m.matchPercent() + "% of ingredients in your pantry";
    }
}
