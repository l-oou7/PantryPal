-- =====================================================================
-- PantryPal MySQL schema (MySQL 8+)
-- Hibernate creates/updates these tables automatically on startup
-- (spring.jpa.hibernate.ddl-auto=update). This script is the same
-- design written by hand, for documentation or a manual setup.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS pantrypal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pantrypal;

CREATE TABLE users (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  display_name           VARCHAR(60)  NOT NULL,
  email                  VARCHAR(120) NOT NULL,
  password_hash          VARCHAR(100) NOT NULL,          -- BCrypt
  expiry_alerts_enabled  BIT          NOT NULL DEFAULT 1,
  expiry_threshold_days  INT          NOT NULL DEFAULT 3,
  celebrations_enabled   BIT          NOT NULL DEFAULT 1,
  created_at             DATETIME(6)  NOT NULL,
  CONSTRAINT uk_users_email UNIQUE (email),
  CONSTRAINT ck_users_threshold CHECK (expiry_threshold_days BETWEEN 1 AND 14)
);

CREATE TABLE dietary_preferences (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT      NOT NULL,
  diet_type  VARCHAR(20) NOT NULL,                        -- GENERAL | VEGETARIAN | VEGAN | KETO
  CONSTRAINT uk_dietpref_user UNIQUE (user_id),
  CONSTRAINT fk_dietpref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE dietary_preference_allergies (
  preference_id BIGINT      NOT NULL,
  allergen      VARCHAR(20) NOT NULL,                     -- DAIRY | EGGS | GLUTEN | PEANUTS | ...
  PRIMARY KEY (preference_id, allergen),
  CONSTRAINT fk_allergy_pref FOREIGN KEY (preference_id) REFERENCES dietary_preferences(id) ON DELETE CASCADE
);

-- Pantry items ("Ingredient" in the brief): one row per stocked item, owned by one user
CREATE TABLE ingredients (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT      NOT NULL,
  name             VARCHAR(80) NOT NULL,
  category         VARCHAR(20) NOT NULL,                  -- FRUITS | VEGETABLES | DAIRY | MEAT_SEAFOOD | GRAINS | SPICES | CANNED_GOODS | OTHERS
  quantity         DOUBLE      NOT NULL,
  unit             VARCHAR(10) NOT NULL,                  -- MG | G | KG | OZ | LB | ML | L | TSP | TBSP | CUP | PCS
  expiration_date  DATE        NOT NULL,
  created_at       DATETIME(6) NOT NULL,
  updated_at       DATETIME(6) NOT NULL,
  CONSTRAINT fk_ingredients_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT ck_ingredients_qty CHECK (quantity > 0),
  INDEX idx_ingredients_user (user_id),
  INDEX idx_ingredients_expiry (expiration_date)
);
-- The 200-items-per-user limit is enforced in PantryService (pantrypal.pantry.max-items).

CREATE TABLE recipes (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  name              VARCHAR(100) NOT NULL,
  description       VARCHAR(600) NOT NULL,
  dietary_category  VARCHAR(20)  NOT NULL,
  course            VARCHAR(20)  NOT NULL,
  cuisine           VARCHAR(40),
  difficulty        VARCHAR(10)  NOT NULL,                -- EASY | MEDIUM | HARD
  prep_minutes      INT NOT NULL,
  cook_minutes      INT NOT NULL,
  servings          INT NOT NULL,
  illustration      VARCHAR(80)  NOT NULL,                -- e.g. "bowl:#F2C14E,#E4572E,#76B041"
  CONSTRAINT uk_recipes_name UNIQUE (name),
  CONSTRAINT ck_recipes_servings CHECK (servings > 0)
);

CREATE TABLE recipe_ingredients (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  recipe_id    BIGINT      NOT NULL,
  name         VARCHAR(80) NOT NULL,
  quantity     DOUBLE      NOT NULL,
  unit         VARCHAR(10) NOT NULL,
  category     VARCHAR(20) NOT NULL,
  is_optional  BIT         NOT NULL DEFAULT 0,
  CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
  INDEX idx_recipe_ingredients_recipe (recipe_id)
);

CREATE TABLE recipe_steps (
  recipe_id    BIGINT       NOT NULL,
  step_order   INT          NOT NULL,
  instruction  VARCHAR(600) NOT NULL,
  PRIMARY KEY (recipe_id, step_order),
  CONSTRAINT fk_steps_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE meal_plans (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  plan_date   DATE        NOT NULL,
  meal_type   VARCHAR(10) NOT NULL,                       -- BREAKFAST | LUNCH | DINNER
  recipe_id   BIGINT      NOT NULL,
  servings    INT         NOT NULL,
  cooked      BIT         NOT NULL DEFAULT 0,
  CONSTRAINT uk_meal_slot UNIQUE (user_id, plan_date, meal_type),
  CONSTRAINT fk_meal_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_meal_plans_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id),
  INDEX idx_meal_plans_user_date (user_id, plan_date)
);

CREATE TABLE shopping_lists (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT      NOT NULL,
  name            VARCHAR(80) NOT NULL,
  generated_from  DATE,
  generated_to    DATE,
  updated_at      DATETIME(6) NOT NULL,
  CONSTRAINT uk_shopping_list_user UNIQUE (user_id),
  CONSTRAINT fk_shopping_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE shopping_list_items (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  shopping_list_id  BIGINT       NOT NULL,
  name              VARCHAR(80)  NOT NULL,
  quantity          DOUBLE       NOT NULL,
  unit              VARCHAR(10)  NOT NULL,
  category          VARCHAR(20)  NOT NULL,
  purchased         BIT          NOT NULL DEFAULT 0,
  source            VARCHAR(10)  NOT NULL,                -- AUTO (from meal plan) | MANUAL
  note              VARCHAR(400),
  CONSTRAINT fk_shopping_items_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE,
  INDEX idx_shopping_items_list (shopping_list_id)
);

CREATE TABLE recipe_bookmarks (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  recipe_id   BIGINT      NOT NULL,
  created_at  DATETIME(6) NOT NULL,
  CONSTRAINT uk_bookmark_user_recipe UNIQUE (user_id, recipe_id),
  CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_bookmarks_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE notifications (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT       NOT NULL,
  ingredient_id    BIGINT,                                -- no FK: alert survives the item being deleted
  ingredient_name  VARCHAR(80)  NOT NULL,
  level            VARCHAR(20)  NOT NULL,                 -- EXPIRED | EXPIRES_TODAY | EXPIRING_SOON
  risk_score       INT          NOT NULL,
  message          VARCHAR(300) NOT NULL,
  is_read          BIT          NOT NULL DEFAULT 0,
  dedupe_key       VARCHAR(120) NOT NULL,                 -- user:item:level:date
  created_at       DATETIME(6)  NOT NULL,
  CONSTRAINT uk_notifications_dedupe UNIQUE (dedupe_key),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_notifications_user (user_id, created_at)
);

-- What happened to pantry food: drives dashboard savings and waste-reduction progress
CREATE TABLE usage_logs (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT      NOT NULL,
  ingredient_name  VARCHAR(80) NOT NULL,
  category         VARCHAR(20) NOT NULL,
  quantity         DOUBLE      NOT NULL,
  unit             VARCHAR(10) NOT NULL,
  action           VARCHAR(10) NOT NULL,                  -- USED | WASTED
  rescued          BIT         NOT NULL DEFAULT 0,        -- used inside the "expiring soon" window
  estimated_value  DOUBLE      NOT NULL,
  logged_at        DATETIME(6) NOT NULL,
  CONSTRAINT fk_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_usage_user (user_id, logged_at)
);
