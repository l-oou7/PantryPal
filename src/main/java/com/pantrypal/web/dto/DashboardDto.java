package com.pantrypal.web.dto;

import java.util.List;

public record DashboardDto(
        String displayName,
        String greeting,
        String mascotMood,
        int totalIngredients,
        int maxIngredients,
        int expiringSoon,
        int expiredCount,
        int cookableNow,
        int almostCookable,
        double estimatedSavings,
        double rescuedValue,
        String currency,
        int wasteReductionPercent,
        long itemsUsed,
        long itemsRescued,
        long itemsWasted,
        long unreadAlerts,
        int thresholdDays,
        List<PantryDtos.IngredientDto> needsLove,
        List<RecipeDtos.RecipeCard> recommended,
        List<PlanDtos.PlannedMeal> todaysMeals) {}
