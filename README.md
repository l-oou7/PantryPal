# PantryPal: smart kitchen management and food waste reduction

> Know what you have. Discover what you can cook. Waste less.

PantryPal is a full-stack web application with a **Java Spring Boot + Spring Data JPA + Spring Security** backend with **MySQL** (or the built-in H2 database for zero-setup local runs) and an **HTML5 / CSS3 / vanilla JavaScript** frontend. The frontend and backend talk only through REST APIs.

It builds on the *Sizzle & Stir: Recipe_OS* OOAD design (Strategy pattern, Observer pattern, SubstitutionEngine, unit normalisation, GRASP roles) and adds what that MVP left out: multi-user authentication, MySQL persistence, the weekly meal planner, automatic shopping lists and scheduled expiry scanning.

---

## 1. Run it

**Requirements:** JDK 17 or newer and Maven (NetBeans includes Maven). **No database to install.**

By default PantryPal uses **H2**, a database built into the app. Data is saved in the `data/` folder inside the project, so it survives restarts. Delete that folder to start fresh.

**NetBeans:** File › Open Project › `pantrypal` › right-click › Run (main class `com.pantrypal.PantryPalApplication`).
**Command line:** `mvn spring-boot:run`

Then open http://localhost:8080.

### Using MySQL instead (as in the project brief)

The same code runs on MySQL 8. Only the connection settings change. They're in `application-mysql.properties`.

1. Install MySQL Server and remember the root password.
2. Run with the `mysql` profile and your password:
   ```bash
   # Windows PowerShell
   $env:DB_PASSWORD="yourpassword"
   mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```
   In NetBeans: Project Properties › Run › VM Options: `-Dspring.profiles.active=mysql -DDB_PASSWORD=yourpassword`.
3. The `pantrypal` database and all tables are created automatically.

For cloud hosting (e.g. Railway), use the `mysql` profile and set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and `PORT` as environment variables on the host.

On first start the app adds **37 recipes** and two demo accounts:

| Email | Password | What it shows |
|---|---|---|
| `demo@pantrypal.app` | `Demo1234` | General diet, 22 pantry items (some expiring today, tomorrow, already expired), meals planned this week, saved recipes, usage history |
| `student@pantrypal.app` | `Student123` | Vegan with a peanut allergy, small pantry. Use it to check that each user's data is separate and that diet filtering works |

Run the unit tests of the recommendation engine with `mvn test`.

Other settings in `application.properties`: expiry-scan cron, default "expiring soon" window, 200-item pantry limit, currency (AED) and seeding on/off.

---|---|---|
| `demo@pantrypal.app` | `Demo1234` | General diet, 22 pantry items (some expiring today, tomorrow, already expired), meals planned this week, saved recipes, usage history |
| `student@pantrypal.app` | `Student123` | Vegan with a peanut allergy, small pantry. Use it to check that each user's data is separate and that diet filtering works |

Run the unit tests of the recommendation engine with `mvn test`.

Configuration lives in `src/main/resources/application.properties`: expiry-scan cron, default "expiring soon" window, 200-item pantry limit, currency (AED) and seeding on/off.

---

## 2. Project structure

```
pantrypal/
├── pom.xml
├── database/schema.sql              hand-written MySQL DDL (same design Hibernate generates)
└── src/main/
    ├── java/com/pantrypal/
    │   ├── PantryPalApplication.java       entry point, @EnableScheduling
    │   ├── domain/        enums and rules with no framework code: Category, Unit, DietType, Allergen,
    │   │                  IngredientKnowledge (vegan/vegetarian/keto rules), TextMatch
    │   ├── engine/        the recommendation engine, plain Java (unit-tested without Spring)
    │   │   ├── strategy/  RecommendationStrategy + 5 concrete strategies + StrategyRegistry
    │   │   ├── MatchCalculator, PantrySnapshot, SubstitutionEngine, SubstitutionRule,
    │   │   ├── UnitNormalizer, Quantity, WasteRiskCalculator, DietaryRules,
    │   │   └── ShoppingCalculator, ConsumptionPlanner, RecommendationEngine (facade)
    │   ├── model/         JPA entities
    │   ├── repository/    Spring Data JPA repositories
    │   ├── service/       business logic, transactions, ExpiryMonitorService (@Scheduled),
    │   │                  ExpiryObserver + NotificationManager (Observer pattern)
    │   ├── security/      UserDetails / UserDetailsService
    │   ├── config/        SecurityConfig, EngineConfig, DataSeeder, SeedRecipes
    │   └── web/           REST controllers, DTOs, GlobalExceptionHandler
    └── resources/
        ├── application.properties
        └── static/        frontend (served by Spring Boot)
            ├── index.html, login.html, dashboard.html, pantry.html, recipes.html,
            │   recipe.html, planner.html, shopping.html, alerts.html, profile.html
            ├── css/style.css
            └── js/  api.js (REST client), ui.js (shell, toasts, modals), art.js (SVG art), pages/*.js
```

**Layers:** Controller → Service → (Engine | Repository) → database (H2 locally, MySQL via profile). Controllers never hold business logic. Services convert entities into the engine's value objects (`EngineMapper`), so the algorithms don't depend on JPA and can be tested on their own.

---

## 3. The recommendation algorithm

### Match percentage (quantity-aware)

Every **required** ingredient is worth one point:

| Pantry has… | Credit |
|---|---|
| enough (after unit conversion, scaled to the chosen servings) | 1 |
| some, but not enough | available ÷ required |
| none | 0 (even if a substitute exists; substitutes are shown separately) |

`match % = round(100 × Σ credit ÷ number of required ingredients)`

- Optional ingredients are ignored. Expired stock never counts.
- Units are normalised first (kg→g, l→ml, tbsp→ml…). If an ingredient is stocked only in an incomparable unit (e.g. garlic in pieces vs. a recipe in grams), it counts as available and the UI says so.
- **Example from the brief:** Vegetable Pasta needs pasta, tomatoes, garlic, olive oil and cheese. With the first four in sufficient quantity: 4/5 = **80%**. With only half the pasta: (0.5 + 3)/5 = **70%**. Both cases are unit tests.

### Strategy Design Pattern

`RecommendationStrategy` is the strategy interface; the engine picks one at runtime by key (`?strategy=`):

| Key | Class | Score |
|---|---|---|
| `balanced` (default) | `BalancedStrategy` | 0.45·match + 0.30·rescue + 0.15·(100 − 30·toBuy) + 0.10·dietBonus |
| `max-match` | `MaxIngredientMatchStrategy` | match % |
| `expiring-first` | `ExpiringIngredientsFirstStrategy` | 0.55·rescue + 0.45·match |
| `dietary` | `DietaryPreferenceStrategy` | match + 30 if the dish is written for the user's diet |
| `min-missing` | `MinimumMissingIngredientsStrategy` | 100 − 25·missing − 10·short + 8·substitutable |

`rescue` (0–100) adds up the waste-risk scores of the near-expiry pantry items a recipe would use. Together the strategies cover the six priorities in the brief: availability, quantities, expiry, diet, missing items, waste.

**Dietary safety is applied before any strategy runs**, so no strategy can ever recommend a conflicting recipe (`DietaryRules`): vegans only get fully plant-based recipes, vegetarians get no meat or seafood, keto users only get keto recipes, and any recipe containing a declared allergen is excluded.

### Substitutions

`SubstitutionEngine` holds about 50 rules (butter→olive oil ×0.75, milk→soy/oat/almond milk, lemon juice→vinegar ×0.5, 1 egg→1 tbsp flaxseed, 1 garlic clove→⅛ tsp garlic powder…). Each suggestion shows the original, the replacement, a recommended quantity (unit-converted) and an explanation. A suggestion is dropped when:
- the replacement breaks the user's diet (no honey for vegans, no chickpeas for keto),
- it contains one of the user's allergens,
- it doesn't suit the dish (no olive oil for butter in baking, no vinegar in desserts, banana-for-egg only in sweet dishes).

Swaps you already have in the pantry are listed first.

### Waste-risk score

```
urgency  = 100 expired · 95 expires today · max(0, 90 − 12·(daysLeft − 1)) otherwise
quantity = min(1, amount / reference)        reference = 1 kg, 1 l or 6 pieces
risk     = urgency × (0.7 + 0.3 × quantity)  (expired = 100)
```
Two items expiring on the same day are ranked by how much food is at stake.

---

## 4. Feature checklist

| Requirement | Where |
|---|---|
| Registration, login, logout, password rules, BCrypt, sessions, CSRF | `AuthController`, `SecurityConfig`, `PasswordPolicy`, `login.html` |
| Per-user data isolation | Every repository query is scoped by `user.id` from the authenticated principal; other users' IDs return 404 |
| Pantry CRUD, search, category filter, units, exact expiry, 200-item limit | `PantryService`, `pantry.html` |
| Use / throw away (feeds savings and waste progress) | `POST /api/pantry/{id}/use`, `/discard`, `UsageLog` |
| Ranked recommendations, strategies, dietary filters | `RecipeService`, `engine/*`, `recipes.html` |
| Recipe details: available / missing / substitutions, servings scaling, bookmarks, "I cooked this" (FIFO pantry deduction) | `recipe.html`, `ConsumptionPlanner` |
| Weekly planner (breakfast/lunch/dinner), edit, remove, match per meal | `MealPlanService`, `planner.html` |
| Shopping list: aggregate → consolidate → subtract pantry → remainder; manual add/edit/remove/tick; move bought items into the pantry | `ShoppingCalculator`, `ShoppingListService`, `shopping.html` |
| Scheduled expiry scan (hourly + at startup), expired / today / soon, deduplicated alerts | `ExpiryMonitorService`, `NotificationManager`, `alerts.html` |
| Dashboard from live data only | `DashboardService` |
| Profile: account, diet, allergies, alert window, notifications, password | `UserService`, `profile.html` |
| Guests can browse recipes from the landing page | `GET /api/recipes` is public |

**Dashboard numbers:** *estimated savings* is the estimated value of pantry food you used instead of buying it again, using average category prices in `PriceEstimator`. *Waste reduction %* = value used ÷ (value used + value thrown away).

---

## 5. REST API

All endpoints return JSON. Errors come back as `{ "status", "message", "fieldErrors"? }`. Change requests need the CSRF header from `GET /api/auth/csrf`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/auth/csrf` | CSRF header name and token |
| POST | `/api/auth/register` · `/api/auth/login` · `/api/auth/logout` | account and session |
| GET | `/api/auth/me` | current user, or 401 |
| GET | `/api/meta` | enum values for forms |
| GET/POST | `/api/pantry` (`?q=&category=`) | list / add |
| PUT/DELETE | `/api/pantry/{id}` | edit / delete |
| POST | `/api/pantry/{id}/use` · `/discard` | log usage or waste |
| GET | `/api/pantry/risk` | items ranked by waste risk |
| GET | `/api/recommendations?strategy=&diet=&limit=` | ranked recommendations |
| GET | `/api/recommendations/strategies` | available strategies |
| GET | `/api/recipes?q=&diet=&course=` | browse/search (public) |
| GET | `/api/recipes/{id}?servings=` | details with availability and substitutions (public) |
| POST | `/api/recipes/{id}/cook` | deduct ingredients from the pantry |
| GET/POST/DELETE | `/api/bookmarks[/{recipeId}]` | saved recipes |
| GET | `/api/meal-plans?date=` | the Mon–Sun week containing `date` |
| POST · PUT/DELETE | `/api/meal-plans` · `/api/meal-plans/{id}` | plan / edit / remove a meal |
| GET | `/api/shopping-list` | current list |
| POST | `/api/shopping-list/generate` `{from,to}` | rebuild from the meal plan |
| POST · PUT/DELETE | `/api/shopping-list/items[/{id}]` | manual items |
| PATCH | `/api/shopping-list/items/{id}/purchased` | tick / untick |
| POST | `/api/shopping-list/clear-purchased` · `/move-to-pantry` | tidy up |
| GET | `/api/alerts` · `/api/alerts/unread-count` | notifications |
| POST | `/api/alerts/{id}/read` · `/read-all` · `/scan` | mark read / scan now |
| GET | `/api/dashboard` | dashboard data |
| GET/PUT | `/api/profile` · `/dietary` · `/notifications` · `/password` | settings |

---

## 6. Design traceability (to the OOAD document)

| Design element | Implementation |
|---|---|
| RecommendationStrategy / DietaryMatchStrategy / WasteReductionStrategy | `engine/strategy/RecommendationStrategy`, `DietaryPreferenceStrategy`, `ExpiringIngredientsFirstStrategy` (+ 3 more) |
| RecommenderController (GRASP Controller, High Cohesion) | `engine/RecommendationEngine` facade + `service/RecipeService` |
| Pantry (Information Expert) | `engine/PantrySnapshot`: availability, expiry and waste risk per ingredient |
| PantryItem.getWasteRiskScore / isExpiringSoon | `engine/WasteRiskCalculator` |
| SubstitutionEngine / SubstitutionRule (Pure Fabrication) | same names in `engine/` |
| Unit normalisation utility | `engine/UnitNormalizer`, `Quantity` |
| ExpiryObserver / NotificationManager (Observer, Low Coupling) | same names in `service/`; `ExpiryMonitorService` is the subject |
| DietaryProfile.isRecipeSafe / HealthRestriction | `engine/DietaryRules`, `model/DietaryPreference` + `domain/Allergen` |
| IngredientStatus (HAVE_ENOUGH, NEED_MORE, MISSING, CAN_SUBSTITUTE, OPTIONAL) | `engine/IngredientStatus` |
| Recipe.scaleRecipe | servings scaling in `MatchCalculator` / `recipe.html` |
| MealPlan.generateGroceryList | `engine/ShoppingCalculator` via `ShoppingListService` |
| RecipeRepository | Spring Data `RecipeRepository` (MySQL instead of in-memory) |

---

## 7. Notes

- Fonts (Fredoka, Nunito, Pixelify Sans) load from Google Fonts; without internet the UI falls back to system fonts.
- All illustrations (chef mascot, kawaii ingredients, dishes, pixel icons) are inline SVG generated by `js/art.js`, so there are no image files to manage.
- Animations respect the operating system's "reduce motion" setting, and celebrations can be turned off in Profile.
