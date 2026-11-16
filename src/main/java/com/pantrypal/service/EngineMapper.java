package com.pantrypal.service;

import com.pantrypal.engine.*;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.Recipe;
import com.pantrypal.model.RecipeIngredient;

import java.time.LocalDate;
import java.util.List;

/** Converts JPA entities into the engine's plain value objects. */
public final class EngineMapper {

    private EngineMapper() {}

    public static StockItem toStock(Ingredient i) {
        return new StockItem(i.getId(), i.getName(), i.getCategory(), i.getQuantity(), i.getUnit(), i.getExpirationDate());
    }

    public static PantrySnapshot snapshot(List<Ingredient> items, LocalDate today) {
        return new PantrySnapshot(items.stream().map(EngineMapper::toStock).toList(), today);
    }

    public static RecipeSpec toSpec(Recipe r) {
        List<RecipeLine> lines = r.getIngredients().stream().map(EngineMapper::toLine).toList();
        return new RecipeSpec(r.getId(), r.getName(), r.getDietaryCategory(), r.getCourse(), r.getServings(), lines);
    }

    private static RecipeLine toLine(RecipeIngredient ri) {
        return new RecipeLine(ri.getName(), ri.getQuantity(), ri.getUnit(), ri.getCategory(), ri.isOptional());
    }
}
