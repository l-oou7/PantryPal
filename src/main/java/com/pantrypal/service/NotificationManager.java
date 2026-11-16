package com.pantrypal.service;

import com.pantrypal.domain.AlertLevel;
import com.pantrypal.model.Ingredient;
import com.pantrypal.model.Notification;
import com.pantrypal.repository.NotificationRepository;
import org.springframework.stereotype.Component;

/** Concrete observer: turns expiry events into in-app notifications, at most one per item, level and day. */
@Component
public class NotificationManager implements ExpiryObserver {

    private final NotificationRepository notifications;

    public NotificationManager(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public boolean onExpiryDetected(ExpiryEvent e) {
        Ingredient i = e.ingredient();
        if (!i.getUser().isExpiryAlertsEnabled()) return false;
        String key = i.getUser().getId() + ":" + i.getId() + ":" + e.level() + ":" + e.scanDate();
        if (notifications.existsByDedupeKey(key)) return false;
        notifications.save(new Notification(i.getUser(), i.getId(), i.getName(), e.level(), e.riskScore(),
                message(i.getName(), e.level(), e.daysLeft()), key));
        return true;
    }

    static String message(String name, AlertLevel level, long daysLeft) {
        return switch (level) {
            case EXPIRED -> name + " has expired. Check it and remove it if it has gone off.";
            case EXPIRES_TODAY -> name + " expires today! Cook it now to save it from the bin.";
            case EXPIRING_SOON -> name + " expires in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s")
                    + ". It needs some love!";
            case FRESH -> name + " is fresh.";
        };
    }
}
