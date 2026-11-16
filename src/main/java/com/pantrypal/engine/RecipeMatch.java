package com.pantrypal.engine;

import java.util.List;

/**
 * Match of one recipe against one pantry. {@code score} and {@code reason} are filled in
 * by whichever recommendation strategy ranked it.
 */
public class RecipeMatch {

    private final RecipeSpec recipe;
    private final int servings;
    private final List<LineAssessment> lines;
    private final int matchPercent;
    private final int fullyAvailable;
    private final int partial;
    private final int missing;
    private final int substitutable;
    private final int requiredCount;
    private final int rescueScore;
    private final int expiringIngredientsUsed;
    private double score;
    private String reason = "";

    public RecipeMatch(RecipeSpec recipe, int servings, List<LineAssessment> lines, int matchPercent,
                       int fullyAvailable, int partial, int missing, int substitutable, int requiredCount,
                       int rescueScore, int expiringIngredientsUsed) {
        this.recipe = recipe;
        this.servings = servings;
        this.lines = List.copyOf(lines);
        this.matchPercent = matchPercent;
        this.fullyAvailable = fullyAvailable;
        this.partial = partial;
        this.missing = missing;
        this.substitutable = substitutable;
        this.requiredCount = requiredCount;
        this.rescueScore = rescueScore;
        this.expiringIngredientsUsed = expiringIngredientsUsed;
    }

    public RecipeSpec recipe() { return recipe; }
    public int servings() { return servings; }
    public List<LineAssessment> lines() { return lines; }
    /** Quantity-weighted ingredient match, 0..100. */
    public int matchPercent() { return matchPercent; }
    public int fullyAvailable() { return fullyAvailable; }
    public int partial() { return partial; }
    /** Required ingredients that are not in the pantry at all (substitutable ones included). */
    public int missing() { return missing; }
    public int substitutable() { return substitutable; }
    public int requiredCount() { return requiredCount; }
    /** 0..100: how much food at risk of expiring this recipe would use up. */
    public int rescueScore() { return rescueScore; }
    public int expiringIngredientsUsed() { return expiringIngredientsUsed; }
    public boolean canCookNow() { return missing == 0 && partial == 0; }
    public double score() { return score; }
    public String reason() { return reason; }

    public void setScore(double score) { this.score = score; }
    public void setReason(String reason) { this.reason = reason; }
}
