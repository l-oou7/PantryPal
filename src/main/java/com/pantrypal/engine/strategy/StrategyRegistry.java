package com.pantrypal.engine.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Looks strategies up by key. New strategies only need to be registered here. */
public class StrategyRegistry {

    public static final String DEFAULT_KEY = "balanced";

    private final Map<String, RecommendationStrategy> strategies = new LinkedHashMap<>();

    public StrategyRegistry() {
        register(new BalancedStrategy());
        register(new MaxIngredientMatchStrategy());
        register(new ExpiringIngredientsFirstStrategy());
        register(new DietaryPreferenceStrategy());
        register(new MinimumMissingIngredientsStrategy());
    }

    public final void register(RecommendationStrategy s) {
        strategies.put(s.key(), s);
    }

    public RecommendationStrategy get(String key) {
        if (key == null || key.isBlank()) return strategies.get(DEFAULT_KEY);
        RecommendationStrategy s = strategies.get(key);
        if (s == null) throw new IllegalArgumentException("Unknown recommendation strategy: " + key);
        return s;
    }

    public List<RecommendationStrategy> all() { return List.copyOf(strategies.values()); }
}
