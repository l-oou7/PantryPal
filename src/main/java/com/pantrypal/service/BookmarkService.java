package com.pantrypal.service;

import com.pantrypal.model.Recipe;
import com.pantrypal.model.RecipeBookmark;
import com.pantrypal.repository.RecipeBookmarkRepository;
import com.pantrypal.repository.RecipeRepository;
import com.pantrypal.repository.UserRepository;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.RecipeDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookmarkService {

    private final RecipeBookmarkRepository bookmarks;
    private final RecipeRepository recipes;
    private final UserRepository users;
    private final RecipeService recipeService;

    public BookmarkService(RecipeBookmarkRepository bookmarks, RecipeRepository recipes, UserRepository users,
                           RecipeService recipeService) {
        this.bookmarks = bookmarks;
        this.recipes = recipes;
        this.users = users;
        this.recipeService = recipeService;
    }

    /** Bookmarked recipes, with live match percentages. */
    @Transactional(readOnly = true)
    public List<RecipeDtos.RecipeCard> list(Long userId) {
        return recipeService.cardsFor(userId, bookmarks.findRecipeIdsByUserId(userId));
    }

    @Transactional
    public void add(Long userId, Long recipeId) {
        if (bookmarks.existsByUserIdAndRecipeId(userId, recipeId)) return;
        Recipe r = recipes.findById(recipeId).orElseThrow(() -> ApiException.notFound("Recipe"));
        bookmarks.save(new RecipeBookmark(users.getReferenceById(userId), r));
    }

    @Transactional
    public void remove(Long userId, Long recipeId) {
        bookmarks.findByUserIdAndRecipeId(userId, recipeId).ifPresent(bookmarks::delete);
    }
}
