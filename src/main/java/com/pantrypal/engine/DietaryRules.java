package com.pantrypal.engine;

import com.pantrypal.domain.Allergen;
import com.pantrypal.domain.DietType;
import com.pantrypal.domain.IngredientKnowledge;

import java.util.Set;

/**
 * Decides whether a recipe is safe for a user. Recipes that fail are never recommended,
 * whichever strategy is selected.
 * <ul>
 *   <li>VEGAN users: every required ingredient must be plant-based.</li>
 *   <li>VEGETARIAN users: no meat or seafood.</li>
 *   <li>KETO users: the recipe must be a keto recipe.</li>
 *   <li>Allergies: no ingredient (optional ones included) may contain a declared allergen.</li>
 * </ul>
 */
public final class DietaryRules {

    private DietaryRules() {}

    public static boolean suitsDiet(RecipeSpec recipe, DietType diet) {
        if (diet == null || diet == DietType.GENERAL) return true;
        if (diet == DietType.KETO) return recipe.dietaryCategory() == DietType.KETO;
        return recipe.requiredLines().stream()
                .allMatch(l -> IngredientKnowledge.suits(diet, l.key(), l.category()));
    }

    public static Allergen firstAllergen(RecipeSpec recipe, Set<Allergen> allergies) {
        if (allergies == null || allergies.isEmpty()) return null;
        for (RecipeLine l : recipe.lines()) {
            for (Allergen a : allergies) {
                if (a.isPresentIn(l.key())) return a;
            }
        }
        return null;
    }

    public static boolean isRecipeSafe(RecipeSpec recipe, RecommendationContext ctx) {
        return suitsDiet(recipe, ctx.diet()) && firstAllergen(recipe, ctx.allergies()) == null;
    }
}
