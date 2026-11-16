package com.pantrypal.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "recipe_bookmarks",
        uniqueConstraints = @UniqueConstraint(name = "uk_bookmark_user_recipe", columnNames = {"user_id", "recipe_id"}))
public class RecipeBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmarks_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmarks_recipe"))
    private Recipe recipe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected RecipeBookmark() {}

    public RecipeBookmark(User user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Recipe getRecipe() { return recipe; }
    public Instant getCreatedAt() { return createdAt; }
}
