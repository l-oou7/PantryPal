package com.pantrypal.engine;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;

/** One ingredient requirement of a recipe, for the recipe's base serving count. */
public record RecipeLine(String name, double amount, Unit unit, Category category, boolean optional) {

    public String key() { return IngredientNames.normalize(name); }

    public Quantity quantity() { return new Quantity(amount, unit); }
}
