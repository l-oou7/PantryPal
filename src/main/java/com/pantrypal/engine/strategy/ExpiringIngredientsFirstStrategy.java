package com.pantrypal.engine.strategy;

import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

import java.util.Comparator;

/**
 * Waste-reduction strategy: favours recipes that use up ingredients close to their expiry date.
 * score = 0.55 x rescueScore + 0.45 x match%
 */
public class ExpiringIngredientsFirstStrategy implements RecommendationStrategy {

    public String key() { return "expiring-first"; }

    public String label() { return "Expiring Ingredients First"; }

    public String description() { return "Use up what's about to go off before it becomes waste."; }

    public double score(RecipeMatch m, RecommendationContext ctx) {
        return 0.55 * m.rescueScore() + 0.45 * m.matchPercent();
    }

    public String reason(RecipeMatch m, RecommendationContext ctx) {
        int n = m.expiringIngredientsUsed();
        if (n > 0) return "Rescues " + n + " ingredient" + (n == 1 ? "" : "s") + " expiring soon";
        if (m.rescueScore() > 0) return "Uses ingredients you should eat this week";
        return "Good match with your pantry";
    }

    @Override
    public Comparator<RecipeMatch> tieBreaker() {
        return Comparator.comparingInt(RecipeMatch::rescueScore).reversed()
                .thenComparing(RecommendationStrategy.super.tieBreaker());
    }
}
