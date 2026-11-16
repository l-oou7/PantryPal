package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.MealPlanService;
import com.pantrypal.web.dto.PlanDtos;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService plans;

    public MealPlanController(MealPlanService plans) {
        this.plans = plans;
    }

    /** The Monday-to-Sunday week containing {@code date} (default: this week). */
    @GetMapping
    public PlanDtos.WeekPlan week(@AuthenticationPrincipal PantryUserDetails me,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return plans.week(me.getId(), date);
    }

    @PostMapping
    public PlanDtos.PlannedMeal assign(@AuthenticationPrincipal PantryUserDetails me,
                                       @Valid @RequestBody PlanDtos.MealPlanRequest req) {
        return plans.assign(me.getId(), req);
    }

    @PutMapping("/{id}")
    public PlanDtos.PlannedMeal update(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id,
                                       @Valid @RequestBody PlanDtos.MealPlanRequest req) {
        return plans.update(me.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id) {
        plans.delete(me.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
