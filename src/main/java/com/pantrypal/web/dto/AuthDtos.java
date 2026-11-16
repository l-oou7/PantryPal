package com.pantrypal.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Please tell us your name.") @Size(max = 60, message = "Name is too long.") String displayName,
            @NotBlank(message = "Email is required.") @Email(message = "That doesn't look like an email address.")
            @Size(max = 120) String email,
            @NotBlank(message = "Password is required.") @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters.")
            String password,
            String confirmPassword) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required.") String email,
            @NotBlank(message = "Password is required.") String password) {}

    public record CurrentUser(Long id, String displayName, String email, String dietType, boolean celebrationsEnabled) {}
}
