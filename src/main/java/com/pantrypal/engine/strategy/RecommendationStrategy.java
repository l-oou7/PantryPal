package com.pantrypal.engine.strategy;

import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;

import java.util.Comparator;
import java.util.List;

/**
 * Strategy interface (Strategy Design Pattern). Each concrete strategy scores already-evaluated
 * recipe matches differently; the engine can swap strategies at runtime without other code changing.
 */
public interface RecommendationStrategy {

    /** Stable identifier used by the REST API, e.g. "max-match". */
    String key();

    String label();

    String description();

    /** Scores one match and writes a short human-readable reason onto it. */
    double score(RecipeMatch match, RecommendationContext ctx);

    String reason(RecipeMatch match, RecommendationContext ctx);

    /** Tie-breakers applied after the score. Default: higher match, then fewer missing, then name. */
    default Comparator<RecipeMatch> tieBreaker() {
        return Comparator.comparingInt(RecipeMatch::matchPercent).reversed()
                .thenComparingInt(RecipeMatch::missing)
                .thenComparing(m -> m.recipe().name());
    }

    /** Scores and sorts, highest score first. */
    default List<RecipeMatch> rank(List<RecipeMatch> matches, RecommendationContext ctx) {
        for (RecipeMatch m : matches) {
            m.setScore(Math.round(score(m, ctx) * 10) / 10.0);
            m.setReason(reason(m, ctx));
        }
        return matches.stream()
                .sorted(Comparator.comparingDouble(RecipeMatch::score).reversed().thenComparing(tieBreaker()))
                .toList();
    }
}
