package com.pantrypal.model;

import com.pantrypal.domain.Category;
import com.pantrypal.domain.Unit;
import jakarta.persistence.*;

/** An ingredient requirement of a recipe, for the recipe's base number of servings. */
@Entity
@Table(name = "recipe_ingredients", indexes = @Index(name = "idx_recipe_ingredients_recipe", columnList = "recipe_id"))
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_recipe_ingredients_recipe"))
    private Recipe recipe;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "is_optional", nullable = false)
    private boolean optional;

    protected RecipeIngredient() {}

    public RecipeIngredient(String name, double quantity, Unit unit, Category category, boolean optional) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.optional = optional;
    }

    public Long getId() { return id; }
    public Recipe getRecipe() { return recipe; }
    void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public Unit getUnit() { return unit; }
    public Category getCategory() { return category; }
    public boolean isOptional() { return optional; }
}
