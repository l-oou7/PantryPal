package com.pantrypal.engine;

import com.pantrypal.domain.AlertLevel;
import com.pantrypal.domain.Dimension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Waste-risk score from 0 (no risk) to 100 (already wasted).
 *
 * <pre>
 * urgency  = 100 if expired, 95 if it expires today,
 *            otherwise max(0, 90 - 12 x (daysLeft - 1))       -> 1 day: 90, 3 days: 66, 8+ days: ~0
 * quantity = min(1, amount / reference)    reference = 1 kg, 1 l or 6 pieces
 * risk     = urgency x (0.7 + 0.3 x quantity)                 (expired items are always 100)
 * </pre>
 * So two items expiring on the same day are ordered by how much food is at stake.
 */
public final class WasteRiskCalculator {

    private WasteRiskCalculator() {}

    public static long daysLeft(LocalDate expiry, LocalDate today) {
        if (expiry == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(today, expiry);
    }

    public static int urgency(long daysLeft) {
        if (daysLeft == Long.MAX_VALUE) return 0;
        if (daysLeft < 0) return 100;
        if (daysLeft == 0) return 95;
        return (int) Math.max(0, 90 - 12 * (daysLeft - 1));
    }

    public static double quantityFactor(double baseAmount, Dimension d) {
        double reference = switch (d) {
            case MASS -> 1000;
            case VOLUME -> 1000;
            case COUNT -> 6;
        };
        return Math.min(1.0, Math.max(0, baseAmount) / reference);
    }

    public static int score(StockItem item, LocalDate today) {
        long d = daysLeft(item.expirationDate(), today);
        if (d < 0) return 100;
        int u = urgency(d);
        double q = quantityFactor(item.baseQuantity(), item.unit().getDimension());
        return (int) Math.round(u * (0.7 + 0.3 * q));
    }

    public static AlertLevel level(LocalDate expiry, LocalDate today, int thresholdDays) {
        long d = daysLeft(expiry, today);
        if (d < 0) return AlertLevel.EXPIRED;
        if (d == 0) return AlertLevel.EXPIRES_TODAY;
        if (d <= thresholdDays) return AlertLevel.EXPIRING_SOON;
        return AlertLevel.FRESH;
    }
}
