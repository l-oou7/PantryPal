package com.pantrypal.web;

import com.pantrypal.domain.Course;
import com.pantrypal.domain.DietType;
import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.RecipeService;
import com.pantrypal.web.dto.RecipeDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeService recipes;

    public RecipeController(RecipeService recipes) {
        this.recipes = recipes;
    }

    /** Browse/search the catalogue. Works for guests; logged-in users also get match percentages. */
    @GetMapping("/recipes")
    public List<RecipeDtos.RecipeCard> browse(@AuthenticationPrincipal PantryUserDetails me,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) DietType diet,
                                              @RequestParam(required = false) Course course) {
        return recipes.browse(me == null ? null : me.getId(), q, diet, course);
    }

    @GetMapping("/recipes/{id}")
    public RecipeDtos.RecipeDetail detail(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                          @RequestParam(required = false) Integer servings) {
        return recipes.detail(me == null ? null : me.getId(), id, servings);
    }

    /** "I cooked this": deducts the ingredients from the pantry. */
    @PostMapping("/recipes/{id}/cook")
    public RecipeDtos.CookResult cook(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                      @RequestBody(required = false) RecipeDtos.CookRequest req) {
        return recipes.cook(me.getId(), id, req == null ? null : req.servings(), req == null ? null : req.mealPlanId());
    }

    /**
     * Ranked recommendations.
     * @param strategy balanced | max-match | expiring-first | dietary | min-missing
     * @param diet     optional extra filter (can only narrow the user's own diet)
     */
    @GetMapping("/recommendations")
    public List<RecipeDtos.RecipeCard> recommend(@AuthenticationPrincipal PantryUserDetails me,
                                                 @RequestParam(required = false) String strategy,
                                                 @RequestParam(required = false) DietType diet,
                                                 @RequestParam(required = false) Integer limit) {
        return recipes.recommend(me.getId(), strategy, diet, limit);
    }

    @GetMapping("/recommendations/strategies")
    public List<RecipeDtos.StrategyDto> strategies() {
        return recipes.strategies();
    }
}
