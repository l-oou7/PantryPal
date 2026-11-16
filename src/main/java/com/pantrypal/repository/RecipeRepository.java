package com.pantrypal.repository;

import com.pantrypal.model.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /** All recipes with their ingredients in one query (what the recommendation engine needs). */
    @EntityGraph(attributePaths = "ingredients")
    List<Recipe> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "ingredients")
    List<Recipe> findByIdIn(List<Long> ids);

    @EntityGraph(attributePaths = "ingredients")
    Optional<Recipe> findWithIngredientsById(Long id);

    boolean existsByNameIgnoreCase(String name);
}
