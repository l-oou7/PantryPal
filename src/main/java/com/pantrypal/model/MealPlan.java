package com.pantrypal.model;

import com.pantrypal.domain.MealType;
import jakarta.persistence.*;

import java.time.LocalDate;

/** A recipe scheduled for one meal slot (breakfast / lunch / dinner) on one day. */
@Entity
@Table(name = "meal_plans",
        uniqueConstraints = @UniqueConstraint(name = "uk_meal_slot", columnNames = {"user_id", "plan_date", "meal_type"}),
        indexes = @Index(name = "idx_meal_plans_user_date", columnList = "user_id, plan_date"))
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meal_plans_user"))
    private User user;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 10)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meal_plans_recipe"))
    private Recipe recipe;

    @Column(nullable = false)
    private int servings;

    @Column(nullable = false)
    private boolean cooked;

    protected MealPlan() {}

    public MealPlan(User user, LocalDate planDate, MealType mealType, Recipe recipe, int servings) {
        this.user = user;
        this.planDate = planDate;
        this.mealType = mealType;
        this.recipe = recipe;
        this.servings = servings;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }
    public boolean isCooked() { return cooked; }
    public void setCooked(boolean cooked) { this.cooked = cooked; }
}
