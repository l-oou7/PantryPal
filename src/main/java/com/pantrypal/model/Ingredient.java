package com.pantrypal.model;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/** One item in a user's digital pantry. */
@Entity
@Table(name = "ingredients", indexes = {
        @Index(name = "idx_ingredients_user", columnList = "user_id"),
        @Index(name = "idx_ingredients_expiry", columnList = "expiration_date")})
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Unit unit;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredients_user"))
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Ingredient() {}

    public Ingredient(User user, String name, Category category, double quantity, Unit unit, LocalDate expirationDate) {
        this.user = user;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expirationDate = expirationDate;
    }

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public User getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
}
