package com.pantrypal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Application settings from application.properties. */
@Component
public class PantryPalProperties {

    @Value("${pantrypal.pantry.max-items:200}")
    private int maxPantryItems;

    @Value("${pantrypal.expiry.default-threshold-days:3}")
    private int defaultThresholdDays;

    @Value("${pantrypal.currency:AED}")
    private String currency;

    public int getMaxPantryItems() { return maxPantryItems; }
    public int getDefaultThresholdDays() { return defaultThresholdDays; }
    public String getCurrency() { return currency; }
}
