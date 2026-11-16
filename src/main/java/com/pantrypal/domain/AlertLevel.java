package com.pantrypal.domain;

/** Freshness state of a pantry item relative to today and the user's "expiring soon" window. */
public enum AlertLevel {
    EXPIRED, EXPIRES_TODAY, EXPIRING_SOON, FRESH
}
