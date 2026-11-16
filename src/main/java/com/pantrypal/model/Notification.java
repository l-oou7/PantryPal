package com.pantrypal.model;

import com.pantrypal.domain.AlertLevel;
import jakarta.persistence.*;

import java.time.Instant;

/** An expiry alert created by the scheduled expiry scanner. */
@Entity
@Table(name = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uk_notifications_dedupe", columnNames = "dedupe_key"),
        indexes = @Index(name = "idx_notifications_user", columnList = "user_id, created_at"))
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    /** Not a foreign key on purpose: the alert stays readable after the pantry item is deleted. */
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "ingredient_name", nullable = false, length = 80)
    private String ingredientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertLevel level;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** user:ingredient:level:date. Prevents the hourly scan from repeating the same alert. */
    @Column(name = "dedupe_key", nullable = false, length = 120)
    private String dedupeKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Notification() {}

    public Notification(User user, Long ingredientId, String ingredientName, AlertLevel level, int riskScore,
                        String message, String dedupeKey) {
        this.user = user;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.level = level;
        this.riskScore = riskScore;
        this.message = message;
        this.dedupeKey = dedupeKey;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public AlertLevel getLevel() { return level; }
    public int getRiskScore() { return riskScore; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getDedupeKey() { return dedupeKey; }
    public Instant getCreatedAt() { return createdAt; }
}
