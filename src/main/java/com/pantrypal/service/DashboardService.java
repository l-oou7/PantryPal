package com.pantrypal.service;

import com.pantrypal.config.PantryPalProperties;
import com.pantrypal.domain.AlertLevel;
import com.pantrypal.model.UsageLog;
import com.pantrypal.model.User;
import com.pantrypal.repository.NotificationRepository;
import com.pantrypal.repository.UsageLogRepository;
import com.pantrypal.web.dto.DashboardDto;
import com.pantrypal.web.dto.PantryDtos;
import com.pantrypal.web.dto.PlanDtos;
import com.pantrypal.web.dto.RecipeDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * Builds the dashboard from live data: pantry rows, recommendation results, usage logs and notifications.
 * <ul>
 *   <li>Estimated savings = estimated value of pantry food you used (cooked or ate) instead of buying again.</li>
 *   <li>Rescued value = the part of that used while it was in the "expiring soon" window.</li>
 *   <li>Waste reduction % = value used / (value used + value thrown away).</li>
 * </ul>
 */
@Service
public class DashboardService {

    private final UserService users;
    private final PantryService pantry;
    private final RecipeService recipes;
    private final MealPlanService plans;
    private final UsageLogRepository usage;
    private final NotificationRepository notifications;
    private final PantryPalProperties props;
    private final CurrentTime clock;

    public DashboardService(UserService users, PantryService pantry, RecipeService recipes, MealPlanService plans,
                            UsageLogRepository usage, NotificationRepository notifications, PantryPalProperties props,
                            CurrentTime clock) {
        this.users = users;
        this.pantry = pantry;
        this.recipes = recipes;
        this.plans = plans;
        this.usage = usage;
        this.notifications = notifications;
        this.props = props;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardDto build(Long userId) {
        User u = users.get(userId);
        List<PantryDtos.IngredientDto> byRisk = pantry.byWasteRisk(userId);
        int expiringSoon = (int) byRisk.stream()
                .filter(i -> i.alertLevel() == AlertLevel.EXPIRING_SOON || i.alertLevel() == AlertLevel.EXPIRES_TODAY).count();
        int expired = (int) byRisk.stream().filter(i -> i.alertLevel() == AlertLevel.EXPIRED).count();
        List<PantryDtos.IngredientDto> needsLove = byRisk.stream()
                .filter(i -> i.alertLevel() != AlertLevel.FRESH).limit(6).toList();

        List<RecipeDtos.RecipeCard> all = recipes.recommend(userId, "balanced", null, null);
        int cookable = (int) all.stream().filter(c -> Boolean.TRUE.equals(c.canCookNow())).count();
        int almost = (int) all.stream().filter(c -> !Boolean.TRUE.equals(c.canCookNow()) && c.matchPercent() >= 75).count();
        List<RecipeDtos.RecipeCard> top = all.stream().limit(4).toList();

        double used = usage.sumValue(userId, UsageLog.Action.USED);
        double wasted = usage.sumValue(userId, UsageLog.Action.WASTED);
        double rescued = usage.sumRescuedValue(userId, UsageLog.Action.USED);
        int wastePct = used + wasted <= 0 ? 0 : (int) Math.round(100 * used / (used + wasted));

        List<PlanDtos.PlannedMeal> today = plans.meals(userId, clock.today(), clock.today());

        String mood = expired > 0 ? "worried" : expiringSoon > 0 ? "alert" : cookable > 0 ? "excited" : "happy";
        return new DashboardDto(u.getDisplayName(), greeting(u.getDisplayName(), expiringSoon, cookable, byRisk.isEmpty()),
                mood, byRisk.size(), props.getMaxPantryItems(), expiringSoon, expired, cookable, almost,
                round2(used), round2(rescued), props.getCurrency(), wastePct,
                usage.countByUserIdAndAction(userId, UsageLog.Action.USED),
                usage.countByUserIdAndActionAndRescuedTrue(userId, UsageLog.Action.USED),
                usage.countByUserIdAndAction(userId, UsageLog.Action.WASTED),
                notifications.countByUserIdAndReadFalse(userId), u.getExpiryThresholdDays(),
                needsLove, top, today);
    }

    private String greeting(String name, int expiring, int cookable, boolean emptyPantry) {
        int h = LocalTime.now().getHour();
        String hello = h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening";
        String first = name.split(" ")[0];
        if (emptyPantry) return hello + ", " + first + "! Your pantry is empty. Let's stock it up together!";
        if (expiring > 0) return hello + ", " + first + "! " + expiring + " ingredient" + (expiring == 1 ? " needs" : "s need")
                + " some love soon. I found recipes to help!";
        if (cookable > 0) return hello + ", " + first + "! You can cook " + cookable + " recipe" + (cookable == 1 ? "" : "s")
                + " right now. Hungry?";
        return hello + ", " + first + "! What shall we cook today?";
    }

    private static double round2(double v) { return Math.round(v * 100) / 100.0; }
}
