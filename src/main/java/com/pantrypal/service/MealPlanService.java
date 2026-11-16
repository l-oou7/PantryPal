package com.pantrypal.service;

import com.pantrypal.domain.MealType;
import com.pantrypal.engine.PantrySnapshot;
import com.pantrypal.engine.RecipeMatch;
import com.pantrypal.engine.RecommendationContext;
import com.pantrypal.engine.RecommendationEngine;
import com.pantrypal.model.MealPlan;
import com.pantrypal.model.Recipe;
import com.pantrypal.repository.MealPlanRepository;
import com.pantrypal.repository.RecipeRepository;
import com.pantrypal.repository.UserRepository;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.PlanDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** Weekly meal planner: one recipe per breakfast/lunch/dinner slot per day. */
@Service
public class MealPlanService {

    private final MealPlanRepository plans;
    private final RecipeRepository recipes;
    private final UserRepository users;
    private final RecipeService recipeService;
    private final PantryService pantry;
    private final RecommendationEngine engine;
    private final CurrentTime clock;

    public MealPlanService(MealPlanRepository plans, RecipeRepository recipes, UserRepository users,
                           RecipeService recipeService, PantryService pantry, RecommendationEngine engine,
                           CurrentTime clock) {
        this.plans = plans;
        this.recipes = recipes;
        this.users = users;
        this.recipeService = recipeService;
        this.pantry = pantry;
        this.engine = engine;
        this.clock = clock;
    }

    public static LocalDate weekStartOf(LocalDate d) {
        return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @Transactional(readOnly = true)
    public PlanDtos.WeekPlan week(Long userId, LocalDate anyDayInWeek) {
        LocalDate start = weekStartOf(anyDayInWeek == null ? clock.today() : anyDayInWeek);
        LocalDate end = start.plusDays(6);
        List<PlanDtos.PlannedMeal> meals = meals(userId, start, end);

        List<PlanDtos.DayPlan> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            Map<MealType, PlanDtos.PlannedMeal> slots = new EnumMap<>(MealType.class);
            for (MealType t : MealType.values()) slots.put(t, null);
            meals.stream().filter(m -> m.date().equals(d)).forEach(m -> slots.put(m.mealType(), m));
            days.add(new PlanDtos.DayPlan(d, d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    d.equals(clock.today()), slots));
        }
        return new PlanDtos.WeekPlan(start, end, days, meals.size());
    }

    /** Planned meals in a date range, each with its current pantry match for the planned servings. */
    @Transactional(readOnly = true)
    public List<PlanDtos.PlannedMeal> meals(Long userId, LocalDate from, LocalDate to) {
        List<MealPlan> list = plans.findRange(userId, from, to);
        if (list.isEmpty()) return List.of();
        RecommendationContext ctx = recipeService.context(userId);
        PantrySnapshot snap = pantry.snapshot(userId);
        Map<Long, Recipe> withIngredients = new HashMap<>();
        recipes.findByIdIn(list.stream().map(m -> m.getRecipe().getId()).distinct().toList())
                .forEach(r -> withIngredients.put(r.getId(), r));
        return list.stream().map(m -> {
            Recipe r = withIngredients.get(m.getRecipe().getId());
            RecipeMatch match = engine.evaluate(EngineMapper.toSpec(r), snap, ctx, m.getServings());
            return new PlanDtos.PlannedMeal(m.getId(), m.getPlanDate(), m.getMealType(), r.getId(), r.getName(),
                    r.getIllustration(), m.getServings(), r.getTotalMinutes(), match.matchPercent(),
                    match.missing() + match.partial(), m.isCooked());
        }).toList();
    }

    /** Puts a recipe in a slot, replacing whatever was there. */
    @Transactional
    public PlanDtos.PlannedMeal assign(Long userId, PlanDtos.MealPlanRequest req) {
        Recipe r = recipes.findById(req.recipeId()).orElseThrow(() -> ApiException.notFound("Recipe"));
        int servings = req.servings() == null ? r.getServings() : req.servings();
        MealPlan mp = plans.findByUserIdAndPlanDateAndMealType(userId, req.date(), req.mealType())
                .orElseGet(() -> new MealPlan(users.getReferenceById(userId), req.date(), req.mealType(), r, servings));
        mp.setRecipe(r);
        mp.setServings(servings);
        mp.setCooked(false);
        plans.save(mp);
        return find(userId, mp.getId(), req.date());
    }

    @Transactional
    public PlanDtos.PlannedMeal update(Long userId, Long id, PlanDtos.MealPlanRequest req) {
        MealPlan mp = plans.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Planned meal"));
        boolean slotChanged = !mp.getPlanDate().equals(req.date()) || mp.getMealType() != req.mealType();
        if (slotChanged) {
            plans.findByUserIdAndPlanDateAndMealType(userId, req.date(), req.mealType()).ifPresent(other -> {
                plans.delete(other);
                plans.flush();
            });
            mp.setPlanDate(req.date());
            mp.setMealType(req.mealType());
        }
        Recipe r = recipes.findById(req.recipeId()).orElseThrow(() -> ApiException.notFound("Recipe"));
        mp.setRecipe(r);
        if (req.servings() != null) mp.setServings(req.servings());
        plans.flush();
        return find(userId, mp.getId(), req.date());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        plans.delete(plans.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Planned meal")));
    }

    private PlanDtos.PlannedMeal find(Long userId, Long id, LocalDate date) {
        return meals(userId, date, date).stream().filter(m -> m.id().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Planned meal"));
    }
}
