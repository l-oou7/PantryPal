package com.pantrypal.model;

import jakarta.persistence.*;

import java.time.Instant;

/** A PantryPal account. Every pantry item, plan, list, bookmark and notification belongs to exactly one user. */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 60)
    private String displayName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Notification preference: create in-app expiry alerts for this user. */
    @Column(name = "expiry_alerts_enabled", nullable = false)
    private boolean expiryAlertsEnabled = true;

    /** Notification preference: how many days ahead an item counts as "expiring soon". */
    @Column(name = "expiry_threshold_days", nullable = false)
    private int expiryThresholdDays = 3;

    /** Notification preference: show celebration animations and friendly messages. */
    @Column(name = "celebrations_enabled", nullable = false)
    private boolean celebrationsEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DietaryPreference dietaryPreference;

    protected User() {}

    public User(String displayName, String email, String passwordHash) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isExpiryAlertsEnabled() { return expiryAlertsEnabled; }
    public void setExpiryAlertsEnabled(boolean v) { this.expiryAlertsEnabled = v; }
    public int getExpiryThresholdDays() { return expiryThresholdDays; }
    public void setExpiryThresholdDays(int v) { this.expiryThresholdDays = v; }
    public boolean isCelebrationsEnabled() { return celebrationsEnabled; }
    public void setCelebrationsEnabled(boolean v) { this.celebrationsEnabled = v; }
    public Instant getCreatedAt() { return createdAt; }
    public DietaryPreference getDietaryPreference() { return dietaryPreference; }

    public void setDietaryPreference(DietaryPreference p) {
        this.dietaryPreference = p;
        if (p != null) p.setUser(this);
    }
}
