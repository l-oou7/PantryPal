package com.pantrypal.web.dto;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ShoppingDtos {

    private ShoppingDtos() {}

    public record ItemRequest(
            @NotBlank(message = "Item name is required.") @Size(max = 80) String name,
            @NotNull(message = "Quantity is required.") @Positive(message = "Quantity must be more than zero.")
            @DecimalMax(value = "100000") Double quantity,
            @NotNull(message = "Pick a unit.") Unit unit,
            Category category,
            Boolean purchased) {}

    public record GenerateRequest(LocalDate from, LocalDate to) {}

    public record ItemDto(Long id, String name, double quantity, Unit unit, String unitSymbol, Category category,
                          String categoryLabel, boolean purchased, String source, String note) {}

    public record ListDto(Long id, String name, LocalDate generatedFrom, LocalDate generatedTo, Instant updatedAt,
                          List<ItemDto> items, int remaining, int purchased) {}

    public record GenerateResult(ListDto list, int added, int plannedMeals, int coveredByPantry, String message) {}
}
