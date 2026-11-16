package com.pantrypal.web.dto;

import com.pantrypal.domain.Allergen;
import com.pantrypal.domain.DietType;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.Set;

public final class ProfileDtos {

    private ProfileDtos() {}

    public record ProfileDto(Long id, String displayName, String email, Instant memberSince, DietType dietType,
                             Set<Allergen> allergies, boolean expiryAlertsEnabled, int expiryThresholdDays,
                             boolean celebrationsEnabled, long pantryCount, long bookmarkCount) {}

    public record AccountRequest(
            @NotBlank(message = "Name can't be empty.") @Size(max = 60, message = "Name is too long.") String displayName) {}

    public record DietaryRequest(@NotNull(message = "Pick a diet.") DietType dietType, Set<Allergen> allergies) {}

    public record NotificationRequest(
            @NotNull Boolean expiryAlertsEnabled,
            @NotNull @Min(value = 1, message = "At least 1 day.") @Max(value = 14, message = "At most 14 days.") Integer expiryThresholdDays,
            @NotNull Boolean celebrationsEnabled) {}

    public record PasswordRequest(
            @NotBlank(message = "Enter your current password.") String currentPassword,
            @NotBlank(message = "Enter a new password.") @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters.")
            String newPassword) {}
}
