package com.pantrypal.model;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Records what happened to pantry food: used (cooked/eaten) or wasted (discarded).
 * Dashboard savings and waste-reduction progress are calculated from these rows.
 */
@Entity
@Table(name = "usage_logs", indexes = @Index(name = "idx_usage_user", columnList = "user_id, logged_at"))
public class UsageLog {

    public enum Action { USED, WASTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_usage_user"))
    private User user;

    @Column(name = "ingredient_name", nullable = false, length = 80)
    private String ingredientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Action action;

    /** Used while it was within the "expiring soon" window, i.e. rescued from waste. */
    @Column(name = "rescued", nullable = false)
    private boolean rescued;

    @Column(name = "estimated_value", nullable = false)
    private double estimatedValue;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt = Instant.now();

    protected UsageLog() {}

    public UsageLog(User user, String ingredientName, Category category, double quantity, Unit unit, Action action,
                    boolean rescued, double estimatedValue) {
        this.user = user;
        this.ingredientName = ingredientName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.action = action;
        this.rescued = rescued;
        this.estimatedValue = estimatedValue;
    }

    public Long getId() { return id; }
    public String getIngredientName() { return ingredientName; }
    public Category getCategory() { return category; }
    public double getQuantity() { return quantity; }
    public Unit getUnit() { return unit; }
    public Action getAction() { return action; }
    public boolean isRescued() { return rescued; }
    public double getEstimatedValue() { return estimatedValue; }
    public Instant getLoggedAt() { return loggedAt; }
}
