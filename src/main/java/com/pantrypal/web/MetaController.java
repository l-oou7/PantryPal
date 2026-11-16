package com.pantrypal.web;

import com.pantrypal.domain.*;
import com.pantrypal.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/** Enum values for the frontend's dropdowns, so the UI and the API can never disagree. */
@RestController
public class MetaController {

    private final RecipeService recipes;

    public MetaController(RecipeService recipes) {
        this.recipes = recipes;
    }

    @GetMapping("/api/meta")
    public Map<String, Object> meta() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("categories", Arrays.stream(Category.values()).map(c -> Map.of("value", c.name(), "label", c.getLabel())).toList());
        m.put("units", Arrays.stream(Unit.values()).map(u -> Map.of("value", u.name(), "label", u.getSymbol(),
                "dimension", u.getDimension().name())).toList());
        m.put("diets", Arrays.stream(DietType.values()).map(d -> Map.of("value", d.name(), "label", d.getLabel())).toList());
        m.put("allergens", Arrays.stream(Allergen.values()).map(a -> Map.of("value", a.name(), "label", a.getLabel())).toList());
        m.put("courses", Arrays.stream(Course.values()).map(Enum::name).toList());
        m.put("mealTypes", Arrays.stream(MealType.values()).map(Enum::name).toList());
        m.put("strategies", recipes.strategies());
        return m;
    }
}
