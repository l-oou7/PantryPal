package com.pantrypal.repository;

import com.pantrypal.model.RecipeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeBookmarkRepository extends JpaRepository<RecipeBookmark, Long> {

    Optional<RecipeBookmark> findByUserIdAndRecipeId(Long userId, Long recipeId);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    long countByUserId(Long userId);

    @Query("select b.recipe.id from RecipeBookmark b where b.user.id = :userId")
    List<Long> findRecipeIdsByUserId(@Param("userId") Long userId);
}
