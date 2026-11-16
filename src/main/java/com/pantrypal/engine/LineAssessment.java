package com.pantrypal.engine;

import java.util.List;

/**
 * Result of checking one recipe ingredient against the pantry.
 *
 * @param required      what the recipe needs for the requested servings
 * @param available     usable stock in the same dimension (null if not stocked or units incomparable)
 * @param shortfall     what is still missing (null when nothing is missing)
 * @param credit        contribution to the match percentage, 0..1
 * @param unitMismatch  stocked, but in units that can't be compared to the recipe's (counted as available)
 * @param wasteRisk     highest waste-risk score of the pantry items this line would use
 */
public record LineAssessment(RecipeLine line, Quantity required, Quantity available, Quantity shortfall,
                             IngredientStatus status, double credit, boolean unitMismatch, int wasteRisk,
                             long daysUntilExpiry, List<Substitution> substitutions) {
}
