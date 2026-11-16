package com.pantrypal.engine;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;

import java.time.LocalDate;

/**
 * Engine-side view of one pantry row. Decouples the algorithms from JPA entities
 * so they can be unit-tested without a database.
 */
public record StockItem(Long id, String name, Category category, double quantity, Unit unit, LocalDate expirationDate) {

    public String key() { return IngredientNames.normalize(name); }

    public double baseQuantity() { return UnitNormalizer.toBase(quantity, unit); }

    public boolean isExpired(LocalDate today) {
        return expirationDate != null && expirationDate.isBefore(today);
    }
}
