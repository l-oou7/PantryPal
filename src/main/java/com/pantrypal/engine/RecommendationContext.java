package com.pantrypal.engine;

import com.pantrypal.domain.Allergen;
import com.pantrypal.domain.DietType;

import java.time.LocalDate;
import java.util.Set;

/** Everything about the user that the engine needs, captured once per request. */
public record RecommendationContext(DietType diet, Set<Allergen> allergies, int expiringThresholdDays, LocalDate today) {

    public RecommendationContext {
        diet = diet == null ? DietType.GENERAL : diet;
        allergies = allergies == null ? Set.of() : Set.copyOf(allergies);
        expiringThresholdDays = Math.max(1, expiringThresholdDays);
        today = today == null ? LocalDate.now() : today;
    }
}
