package com.pantrypal.web.dto;

import com.pantrypal.domain.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class PlanDtos {

    private PlanDtos() {}

    public record MealPlanRequest(
            @NotNull(message = "Pick a day.") LocalDate date,
            @NotNull(message = "Pick breakfast, lunch or dinner.") MealType mealType,
            @NotNull(message = "Pick a recipe.") Long recipeId,
            @Min(value = 1, message = "At least 1 serving.") @Max(value = 20, message = "At most 20 servings.") Integer servings) {}

    public record PlannedMeal(Long id, LocalDate date, MealType mealType, Long recipeId, String recipeName,
                              String illustration, int servings, int totalMinutes, int matchPercent, int missingCount,
                              boolean cooked) {}

    public record DayPlan(LocalDate date, String dayName, boolean today, Map<MealType, PlannedMeal> meals) {}

    public record WeekPlan(LocalDate weekStart, LocalDate weekEnd, List<DayPlan> days, int plannedCount) {}
}
