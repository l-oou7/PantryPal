package com.pantrypal.service;

import com.pantrypal.domain.Course;
import com.pantrypal.domain.DietType;
import com.pantrypal.engine.*;
import com.pantrypal.model.*;
import com.pantrypal.repository.*;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.RecipeDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Recipe browsing, recommendations, recipe details and "cook this" (pantry deduction).
 * Delegates all matching and ranking to the framework-free {@link RecommendationEngine}.
 */
@Service
public class RecipeService {

    public static final String MATCH_EXPLANATION =
            "Each required ingredient counts as 1 point. You get the full point if your pantry has enough of it "
            + "(units are converted automatically), a partial point if you have some (e.g. half the pasta = 0.5), "
            + "and nothing if it is missing. Match % = points / required ingredients x 100. "
            + "Optional ingredients and expired items don't count.";

    private final RecipeRepository recipes;
    private final RecipeBookmarkRepository bookmarks;
    private final IngredientRepository ingredients;
    private final MealPlanRepository mealPlans;
    private final UsageLogRepository usage;
    private final UserService userService;
    private final PantryService pantry;
    private final RecommendationEngine engine;
    private final PriceEstimator prices;
    private final CurrentTime clock;

    public RecipeService(RecipeRepository recipes, RecipeBookmarkRepository bookmarks, IngredientRepository ingredients,
                         MealPlanRepository mealPlans, UsageLogRepository usage, UserService userService,
                         PantryService pantry, RecommendationEngine engine, PriceEstimator prices, CurrentTime clock) {
        this.recipes = recipes;
        this.bookmarks = bookmarks;
        this.ingredients = ingredients;
        this.mealPlans = mealPlans;
        this.usage = usage;
        this.userService = userService;
        this.pantry = pantry;
        this.engine = engine;
        this.prices = prices;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ context

    @Transactional(readOnly = true)
    public RecommendationContext context(Long userId) {
        User u = userService.get(userId);
        DietaryPreference p = userService.preference(u);
        return new RecommendationContext(p.getDietType(), p.getAllergies(), u.getExpiryThresholdDays(), clock.today());
    }

    public List<RecipeDtos.StrategyDto> strategies() {
        return engine.registry().all().stream()
                .map(s -> new RecipeDtos.StrategyDto(s.key(), s.label(), s.description())).toList();
    }

    // ------------------------------------------------------------------ recommendations

    @Transactional(readOnly = true)
    public List<RecipeDtos.RecipeCard> recommend(Long userId, String strategyKey, DietType dietFilter, Integer limit) {
        RecommendationContext ctx = context(userId);
        PantrySnapshot snap = pantry.snapshot(userId);
        Map<Long, Recipe> byId = new LinkedHashMap<>();
        List<RecipeSpec> specs = new ArrayList<>();
        for (Recipe r : recipes.findAllByOrderByNameAsc()) {
            byId.put(r.getId(), r);
            specs.add(EngineMapper.toSpec(r));
        }
        List<RecipeMatch> ranked = engine.recommend(specs, snap, ctx, strategyKey, dietFilter);
        Set<Long> saved = new HashSet<>(bookmarks.findRecipeIdsByUserId(userId));
        return ranked.stream()
                .limit(limit == null || limit <= 0 ? Long.MAX_VALUE : limit)
                .map(m -> card(byId.get(m.recipe().id()), m, saved.contains(m.recipe().id())))
                .toList();
    }

    // ------------------------------------------------------------------ browsing

    /**
     * Searches by recipe name or ingredient name (FR-14) and filters by diet / course.
     * Logged-in users also get their match % and never see recipes that conflict with their diet.
     */
    @Transactional(readOnly = true)
    public List<RecipeDtos.RecipeCard> browse(Long userIdOrNull, String query, DietType diet, Course course) {
        String q = query == null ? "" : IngredientNames.normalize(query);
        String rawQ = query == null ? "" : query.trim().toLowerCase();
        RecommendationContext ctx = userIdOrNull == null ? null : context(userIdOrNull);
        PantrySnapshot snap = userIdOrNull == null ? null : pantry.snapshot(userIdOrNull);
        Set<Long> saved = userIdOrNull == null ? Set.of() : new HashSet<>(bookmarks.findRecipeIdsByUserId(userIdOrNull));

        List<RecipeDtos.RecipeCard> out = new ArrayList<>();
        for (Recipe r : recipes.findAllByOrderByNameAsc()) {
            RecipeSpec spec = EngineMapper.toSpec(r);
            if (diet != null && diet != DietType.GENERAL && !DietaryRules.suitsDiet(spec, diet)) continue;
            if (course != null && r.getCourse() != course) continue;
            if (!rawQ.isEmpty()) {
                boolean hit = r.getName().toLowerCase().contains(rawQ)
                        || (r.getCuisine() != null && r.getCuisine().toLowerCase().contains(rawQ))
                        || spec.lines().stream().anyMatch(l -> l.key().contains(q));
                if (!hit) continue;
            }
            if (ctx != null) {
                if (!DietaryRules.isRecipeSafe(spec, ctx)) continue;
                RecipeMatch m = engine.evaluate(spec, snap, ctx, null);
                out.add(card(r, m, saved.contains(r.getId())));
            } else {
                out.add(card(r, null, false));
            }
        }
        if (ctx != null) {
            out.sort(Comparator.comparing((RecipeDtos.RecipeCard c) -> -c.matchPercent()).thenComparing(RecipeDtos.RecipeCard::name));
        }
        return out;
    }

    /** Cards with live match % for specific recipes (bookmarks), regardless of current diet filter. */
    @Transactional(readOnly = true)
    public List<RecipeDtos.RecipeCard> cardsFor(Long userId, Collection<Long> recipeIds) {
        if (recipeIds.isEmpty()) return List.of();
        RecommendationContext ctx = context(userId);
        PantrySnapshot snap = pantry.snapshot(userId);
        Set<Long> saved = new HashSet<>(bookmarks.findRecipeIdsByUserId(userId));
        return recipes.findByIdIn(new ArrayList<>(recipeIds)).stream()
                .map(r -> {
                    RecipeSpec spec = EngineMapper.toSpec(r);
                    RecipeDtos.RecipeCard c = card(r, engine.evaluate(spec, snap, ctx, null), saved.contains(r.getId()));
                    return DietaryRules.isRecipeSafe(spec, ctx) ? c
                            : withReason(c, "Doesn't fit your current dietary preferences");
                })
                .sorted(Comparator.comparing(RecipeDtos.RecipeCard::name))
                .toList();
    }

    // ------------------------------------------------------------------ detail

    @Transactional(readOnly = true)
    public RecipeDtos.RecipeDetail detail(Long userIdOrNull, Long recipeId, Integer servings) {
        Recipe r = recipes.findWithIngredientsById(recipeId).orElseThrow(() -> ApiException.notFound("Recipe"));
        RecipeSpec spec = EngineMapper.toSpec(r);
        List<String> steps = new ArrayList<>(r.getSteps());
        int target = servings == null || servings <= 0 ? r.getServings() : Math.min(servings, 50);

        if (userIdOrNull == null) {
            double factor = (double) target / r.getServings();
            List<RecipeDtos.IngredientLine> all = spec.lines().stream().map(l -> new RecipeDtos.IngredientLine(
                    IngredientNames.display(l.name()), l.quantity().scale(factor).format(), null, null,
                    null, l.optional(), false, false, List.of())).toList();
            return new RecipeDtos.RecipeDetail(card(r, null, false), target, List.of(), List.of(), List.of(), all, steps,
                    MATCH_EXPLANATION);
        }

        RecommendationContext ctx = context(userIdOrNull);
        RecipeMatch m = engine.evaluate(spec, pantry.snapshot(userIdOrNull), ctx, target);
        boolean saved = bookmarks.existsByUserIdAndRecipeId(userIdOrNull, recipeId);

        List<RecipeDtos.IngredientLine> available = new ArrayList<>();
        List<RecipeDtos.IngredientLine> missing = new ArrayList<>();
        List<RecipeDtos.SubstitutionDto> subs = new ArrayList<>();
        List<RecipeDtos.IngredientLine> all = new ArrayList<>();
        for (LineAssessment la : m.lines()) {
            RecipeDtos.IngredientLine line = line(la, ctx);
            all.add(line);
            if (la.status() == IngredientStatus.HAVE_ENOUGH) available.add(line);
            else missing.add(line);
            subs.addAll(line.substitutions());
        }
        RecipeDtos.RecipeCard card = card(r, m, saved);
        if (!DietaryRules.isRecipeSafe(spec, ctx)) {
            card = withReason(card, "Heads up: this recipe doesn't fit your dietary preferences.");
        }
        return new RecipeDtos.RecipeDetail(card, target, available, missing, subs, all, steps, MATCH_EXPLANATION);
    }

    // ------------------------------------------------------------------ cooking

    /** Deducts the recipe's ingredients from the pantry (earliest expiry first) and logs the usage. */
    @Transactional
    public RecipeDtos.CookResult cook(Long userId, Long recipeId, Integer servings, Long mealPlanId) {
        Recipe r = recipes.findWithIngredientsById(recipeId).orElseThrow(() -> ApiException.notFound("Recipe"));
        RecipeSpec spec = EngineMapper.toSpec(r);
        int target = servings == null || servings <= 0 ? r.getServings() : servings;
        List<Ingredient> stock = ingredients.findByUserIdOrderByExpirationDateAsc(userId);
        Map<Long, Ingredient> byId = new HashMap<>();
        stock.forEach(i -> byId.put(i.getId(), i));
        LocalDate today = clock.today();
        PantrySnapshot snap = EngineMapper.snapshot(stock, today);
        int threshold = pantry.threshold(userId);

        List<ConsumptionPlanner.Deduction> plan = ConsumptionPlanner.plan(spec, target, snap);
        if (plan.isEmpty()) {
            throw ApiException.badRequest("None of this recipe's ingredients are in your pantry yet.");
        }
        int rescued = 0;
        double value = 0;
        List<String> used = new ArrayList<>();
        for (ConsumptionPlanner.Deduction d : plan) {
            Ingredient i = byId.get(d.stockId());
            boolean isRescue = d.daysUntilExpiry() >= 0 && d.daysUntilExpiry() <= threshold;
            double v = prices.estimate(i.getCategory(), d.amount(), i.getUnit());
            usage.save(new UsageLog(i.getUser(), i.getName(), i.getCategory(), d.amount(), i.getUnit(),
                    UsageLog.Action.USED, isRescue, v));
            value += v;
            if (isRescue) rescued++;
            used.add(new Quantity(Math.round(d.amount() * 100) / 100.0, i.getUnit()).format() + " " + i.getName());
            if (d.remaining() <= 0) ingredients.delete(i);
            else i.setQuantity(Math.round(d.remaining() * 1000) / 1000.0);
        }
        if (mealPlanId != null) {
            mealPlans.findByIdAndUserId(mealPlanId, userId).ifPresent(mp -> mp.setCooked(true));
        }
        String msg = rescued > 0
                ? "Nice work! You saved " + rescued + " ingredient" + (rescued == 1 ? "" : "s") + " from going to waste!"
                : "Bon appétit! Your pantry has been updated.";
        return new RecipeDtos.CookResult(msg, plan.size(), rescued, Math.round(value * 100) / 100.0, used);
    }

    // ------------------------------------------------------------------ mapping

    public static RecipeDtos.RecipeCard card(Recipe r, RecipeMatch m, boolean bookmarked) {
        return new RecipeDtos.RecipeCard(r.getId(), r.getName(), r.getDescription(), r.getDietaryCategory(), r.getCourse(),
                r.getCuisine(), r.getDifficulty(), r.getPrepMinutes(), r.getCookMinutes(), r.getTotalMinutes(),
                r.getServings(), r.getIllustration(),
                m == null ? null : m.matchPercent(),
                m == null ? null : m.fullyAvailable(),
                m == null ? null : m.requiredCount(),
                m == null ? null : m.missing() + m.partial(),
                m == null ? null : m.expiringIngredientsUsed(),
                m == null ? null : m.canCookNow(),
                m == null ? null : m.score(),
                m == null ? null : m.reason(),
                bookmarked);
    }

    private static RecipeDtos.RecipeCard withReason(RecipeDtos.RecipeCard c, String reason) {
        return new RecipeDtos.RecipeCard(c.id(), c.name(), c.description(), c.dietaryCategory(), c.course(), c.cuisine(),
                c.difficulty(), c.prepMinutes(), c.cookMinutes(), c.totalMinutes(), c.servings(), c.illustration(),
                c.matchPercent(), c.haveCount(), c.requiredCount(), c.missingCount(), c.expiringUsed(), c.canCookNow(),
                c.score(), reason, c.bookmarked());
    }

    private static RecipeDtos.IngredientLine line(LineAssessment la, RecommendationContext ctx) {
        List<RecipeDtos.SubstitutionDto> subs = la.substitutions().stream()
                .map(s -> new RecipeDtos.SubstitutionDto(s.original(), s.replacement(), s.recommendedQuantity().format(),
                        s.explanation(), s.inPantry()))
                .toList();
        boolean expiring = la.daysUntilExpiry() != Long.MAX_VALUE && la.daysUntilExpiry() <= ctx.expiringThresholdDays();
        return new RecipeDtos.IngredientLine(IngredientNames.display(la.line().name()), la.required().format(),
                la.available() == null ? null : la.available().format(),
                la.shortfall() == null ? null : la.shortfall().format(),
                la.status(), la.line().optional(), la.unitMismatch(), expiring, subs);
    }
}
