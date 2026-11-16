package com.pantrypal.service;

import com.pantrypal.web.ApiException;

/** Password rules: 8-72 characters with at least one letter and one number. */
public final class PasswordPolicy {

    private PasswordPolicy() {}

    public static void check(String password) {
        if (password == null || password.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters.");
        }
        if (password.length() > 72) {
            throw ApiException.badRequest("Password must be at most 72 characters.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw ApiException.badRequest("Password needs at least one letter and one number.");
        }
    }
}
