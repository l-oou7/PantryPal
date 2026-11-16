package com.pantrypal.service;

import com.pantrypal.domain.AlertLevel;
import com.pantrypal.model.Ingredient;

import java.time.LocalDate;

/** Published by the expiry scanner for every pantry item that needs attention. */
public record ExpiryEvent(Ingredient ingredient, AlertLevel level, long daysLeft, int riskScore, LocalDate scanDate) {}
