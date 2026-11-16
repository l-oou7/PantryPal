package com.pantrypal.repository;

import com.pantrypal.domain.Category;
import com.pantrypal.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByUserIdOrderByExpirationDateAsc(Long userId);

    Optional<Ingredient> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("""
           select i from Ingredient i
           where i.user.id = :userId
             and (:category is null or i.category = :category)
             and (:search is null or lower(i.name) like :search)
           order by i.expirationDate asc, i.name asc""")
    List<Ingredient> search(@Param("userId") Long userId, @Param("category") Category category,
                            @Param("search") String searchLikePattern);

    /** For the scheduled scanner: everything that expires on or before the given date, across all users. */
    @Query("select i from Ingredient i join fetch i.user where i.expirationDate <= :until")
    List<Ingredient> findExpiringOnOrBefore(@Param("until") LocalDate until);
}
