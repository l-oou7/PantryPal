package com.pantrypal.web.dto;

import com.pantrypal.domain.AlertLevel;
import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public final class PantryDtos {

    private PantryDtos() {}

    public record IngredientRequest(
            @NotBlank(message = "Ingredient name is required.") @Size(max = 80, message = "Name is too long.") String name,
            @NotNull(message = "Pick a category.") Category category,
            @NotNull(message = "Quantity is required.") @Positive(message = "Quantity must be more than zero.")
            @DecimalMax(value = "100000", message = "That quantity is too large.") Double quantity,
            @NotNull(message = "Pick a unit.") Unit unit,
            @NotNull(message = "Expiration date is required.") LocalDate expirationDate) {}

    public record UseRequest(
            @NotNull(message = "How much did you use?") @Positive(message = "Amount must be more than zero.") Double quantity) {}

    public record IngredientDto(Long id, String name, Category category, String categoryLabel, double quantity, Unit unit,
                                String unitSymbol, LocalDate expirationDate, long daysLeft, AlertLevel alertLevel,
                                int wasteRisk, Long userId) {}

    /** Response of use/discard: the updated item (null if used up) plus a friendly message. */
    public record ActionResult(IngredientDto item, String message, boolean rescued, double valueSaved) {}
}
