package com.pantrypal.model;

import com.pantrypal.domain.Course;
import com.pantrypal.domain.DietType;
import com.pantrypal.domain.Difficulty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/** A recipe in the shared recipe catalogue. */
@Entity
@Table(name = "recipes", uniqueConstraints = @UniqueConstraint(name = "uk_recipes_name", columnNames = "name"))
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 600)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_category", nullable = false, length = 20)
    private DietType dietaryCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Course course;

    @Column(length = 40)
    private String cuisine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(name = "prep_minutes", nullable = false)
    private int prepMinutes;

    @Column(name = "cook_minutes", nullable = false)
    private int cookMinutes;

    @Column(nullable = false)
    private int servings;

    /** Illustration recipe for the frontend, e.g. "bowl:#F4A259,#E76F51,#8AB17D". */
    @Column(nullable = false, length = 80)
    private String illustration;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "recipe_steps",
            joinColumns = @JoinColumn(name = "recipe_id", foreignKey = @ForeignKey(name = "fk_steps_recipe")))
    @OrderColumn(name = "step_order")
    @Column(name = "instruction", nullable = false, length = 600)
    private List<String> steps = new ArrayList<>();

    protected Recipe() {}

    public Recipe(String name, String description, DietType dietaryCategory, Course course, String cuisine,
                  Difficulty difficulty, int prepMinutes, int cookMinutes, int servings, String illustration) {
        this.name = name;
        this.description = description;
        this.dietaryCategory = dietaryCategory;
        this.course = course;
        this.cuisine = cuisine;
        this.difficulty = difficulty;
        this.prepMinutes = prepMinutes;
        this.cookMinutes = cookMinutes;
        this.servings = servings;
        this.illustration = illustration;
    }

    public void addIngredient(RecipeIngredient ri) {
        ri.setRecipe(this);
        ingredients.add(ri);
    }

    public void addStep(String step) { steps.add(step); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DietType getDietaryCategory() { return dietaryCategory; }
    public Course getCourse() { return course; }
    public String getCuisine() { return cuisine; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getPrepMinutes() { return prepMinutes; }
    public int getCookMinutes() { return cookMinutes; }
    public int getTotalMinutes() { return prepMinutes + cookMinutes; }
    public int getServings() { return servings; }
    public String getIllustration() { return illustration; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
}
