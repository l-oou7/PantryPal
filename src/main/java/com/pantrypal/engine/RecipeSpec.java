package com.pantrypal.engine;

import com.pantrypal.domain.Course;
import com.pantrypal.domain.DietType;

import java.util.List;

/** Engine-side view of a recipe. */
public record RecipeSpec(long id, String name, DietType dietaryCategory, Course course, int servings,
                         List<RecipeLine> lines) {

    public List<RecipeLine> requiredLines() {
        return lines.stream().filter(l -> !l.optional()).toList();
    }
}
