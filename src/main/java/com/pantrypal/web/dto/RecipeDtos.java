package com.pantrypal.web.dto;

import com.pantrypal.domain.Course;
import com.pantrypal.domain.DietType;
import com.pantrypal.domain.Difficulty;
import com.pantrypal.engine.IngredientStatus;

import java.util.List;

public final class RecipeDtos {

    private RecipeDtos() {}

    /** Card shown in lists. Match fields are null for guests. */
    public record RecipeCard(Long id, String name, String description, DietType dietaryCategory, Course course,
                             String cuisine, Difficulty difficulty, int prepMinutes, int cookMinutes, int totalMinutes,
                             int servings, String illustration, Integer matchPercent, Integer haveCount,
                             Integer requiredCount, Integer missingCount, Integer expiringUsed, Boolean canCookNow,
                             Double score, String reason, boolean bookmarked) {}

    public record SubstitutionDto(String original, String replacement, String recommendedQuantity, String explanation,
                                  boolean inPantry) {}

    public record IngredientLine(String name, String required, String available, String shortfall,
                                 IngredientStatus status, boolean optional, boolean unitMismatch, boolean expiringSoon,
                                 List<SubstitutionDto> substitutions) {}

    public record RecipeDetail(RecipeCard card, int requestedServings, List<IngredientLine> available,
                               List<IngredientLine> missing, List<SubstitutionDto> substitutions,
                               List<IngredientLine> allIngredients, List<String> steps, String matchExplanation) {}

    public record StrategyDto(String key, String label, String description) {}

    public record CookRequest(Integer servings, Long mealPlanId) {}

    public record CookResult(String message, int itemsUsed, int itemsRescued, double valueSaved, List<String> used) {}
}
