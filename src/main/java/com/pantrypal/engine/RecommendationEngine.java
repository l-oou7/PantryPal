package com.pantrypal.engine;

import com.pantrypal.domain.DietType;
import com.pantrypal.engine.strategy.RecommendationStrategy;
import com.pantrypal.engine.strategy.StrategyRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade over matching, dietary filtering and strategy ranking
 * (the "RecommenderController" of the design document).
 */
public class RecommendationEngine {

    private final MatchCalculator calculator;
    private final StrategyRegistry registry;

    public RecommendationEngine(MatchCalculator calculator, StrategyRegistry registry) {
        this.calculator = calculator;
        this.registry = registry;
    }

    public StrategyRegistry registry() { return registry; }

    /**
     * @param dietFilter optional extra filter chosen in the UI (e.g. browse only Vegan dishes).
     *                   It can narrow results but never widen past the user's own diet.
     */
    public List<RecipeMatch> recommend(List<RecipeSpec> recipes, PantrySnapshot pantry, RecommendationContext ctx,
                                       String strategyKey, DietType dietFilter) {
        RecommendationStrategy strategy = registry.get(strategyKey);
        List<RecipeMatch> matches = new ArrayList<>();
        for (RecipeSpec r : recipes) {
            if (!DietaryRules.isRecipeSafe(r, ctx)) continue;
            if (dietFilter != null && dietFilter != DietType.GENERAL && !DietaryRules.suitsDiet(r, dietFilter)) continue;
            matches.add(calculator.evaluate(r, pantry, ctx, null));
        }
        return strategy.rank(matches, ctx);
    }

    public RecipeMatch evaluate(RecipeSpec recipe, PantrySnapshot pantry, RecommendationContext ctx, Integer servings) {
        return calculator.evaluate(recipe, pantry, ctx, servings);
    }
}
