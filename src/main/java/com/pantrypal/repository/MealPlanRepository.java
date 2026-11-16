package com.pantrypal.repository;

import com.pantrypal.domain.MealType;
import com.pantrypal.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    @Query("""
           select m from MealPlan m join fetch m.recipe r
           where m.user.id = :userId and m.planDate between :from and :to
           order by m.planDate asc, m.mealType asc""")
    List<MealPlan> findRange(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    Optional<MealPlan> findByIdAndUserId(Long id, Long userId);

    Optional<MealPlan> findByUserIdAndPlanDateAndMealType(Long userId, LocalDate date, MealType mealType);
}
