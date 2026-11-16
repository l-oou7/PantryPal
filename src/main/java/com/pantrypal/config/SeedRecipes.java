package com.pantrypal.config;

import com.pantrypal.domain.*;
import com.pantrypal.model.Recipe;
import com.pantrypal.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

import static com.pantrypal.domain.Category.*;
import static com.pantrypal.domain.Course.*;
import static com.pantrypal.domain.DietType.*;
import static com.pantrypal.domain.Difficulty.*;
import static com.pantrypal.domain.Unit.*;

/** Sample recipe catalogue (quantities are for the stated servings). */
final class SeedRecipes {

    private SeedRecipes() {}

    private static final class B {
        final Recipe r;
        B(String name, String desc, DietType diet, Course course, String cuisine, Difficulty d, int prep, int cook,
          int servings, String art) {
            r = new Recipe(name, desc, diet, course, cuisine, d, prep, cook, servings, art);
        }
        B i(String n, double q, Unit u, Category c) { r.addIngredient(new RecipeIngredient(n, q, u, c, false)); return this; }
        B o(String n, double q, Unit u, Category c) { r.addIngredient(new RecipeIngredient(n, q, u, c, true)); return this; }
        B s(String... steps) { for (String st : steps) r.addStep(st); return this; }
    }

    private static B r(String name, String desc, DietType diet, Course course, String cuisine, Difficulty d,
                       int prep, int cook, int servings, String art) {
        return new B(name, desc, diet, course, cuisine, d, prep, cook, servings, art);
    }

    static List<Recipe> all() {
        List<B> b = new ArrayList<>();

        b.add(r("Vegetable Pasta", "A quick, garlicky tomato pasta finished with a shower of cheese. The classic weeknight rescue for soft tomatoes.",
                VEGETARIAN, MAIN, "Italian", EASY, 10, 15, 2, "bowl:#F2C14E,#E4572E,#76B041")
                .i("Pasta", 200, G, GRAINS).i("Tomato", 3, PCS, VEGETABLES).i("Garlic", 2, PCS, VEGETABLES)
                .i("Olive oil", 2, TBSP, OTHERS).i("Cheese", 40, G, DAIRY).o("Basil", 5, G, SPICES)
                .s("Boil the pasta in well-salted water until al dente. Save a cup of the cooking water.",
                   "Meanwhile, warm the olive oil and gently fry the sliced garlic for 1 minute.",
                   "Add the chopped tomatoes and a pinch of salt; simmer for 8 minutes until saucy.",
                   "Toss in the pasta with a splash of pasta water, then finish with cheese and torn basil."));

        b.add(r("Creamy Mushroom Pasta", "Silky mushroom sauce clinging to every strand. Comfort in 25 minutes.",
                VEGETARIAN, MAIN, "Italian", EASY, 10, 15, 2, "bowl:#E9D8A6,#9C6644,#F2C14E")
                .i("Pasta", 200, G, GRAINS).i("Mushroom", 250, G, VEGETABLES).i("Milk", 200, ML, DAIRY)
                .i("Garlic", 2, PCS, VEGETABLES).i("Butter", 20, G, DAIRY).i("Parmesan", 30, G, DAIRY)
                .s("Cook the pasta until al dente.",
                   "Melt the butter and fry the sliced mushrooms over high heat until golden.",
                   "Add the garlic for 1 minute, then pour in the milk and simmer until slightly thickened.",
                   "Stir in the parmesan, add the pasta and toss. Season with pepper."));

        b.add(r("Tomato Basil Soup", "Velvety roasted tomato soup. Perfect for tomatoes that are getting a little too soft.",
                VEGAN, SOUP, "Italian", EASY, 10, 30, 4, "pot:#E4572E,#F28F3B,#76B041")
                .i("Tomato", 8, PCS, VEGETABLES).i("Onion", 1, PCS, VEGETABLES).i("Garlic", 3, PCS, VEGETABLES)
                .i("Vegetable broth", 500, ML, CANNED_GOODS).i("Olive oil", 2, TBSP, OTHERS).o("Basil", 10, G, SPICES)
                .s("Soften the chopped onion in olive oil for 6 minutes. Add the garlic for 1 minute.",
                   "Add the chopped tomatoes and broth; simmer for 20 minutes.",
                   "Blend until smooth, season, and stir through torn basil."));

        b.add(r("Spinach & Feta Omelette", "Fluffy eggs folded around wilted spinach and salty feta.",
                VEGETARIAN, BREAKFAST, "Greek", EASY, 5, 8, 1, "pan:#F9DC5C,#76B041,#FFFFFF")
                .i("Egg", 3, PCS, DAIRY).i("Spinach", 50, G, VEGETABLES).i("Feta", 30, G, DAIRY).i("Butter", 10, G, DAIRY)
                .s("Whisk the eggs with a pinch of salt and pepper.",
                   "Melt the butter in a non-stick pan and wilt the spinach for 1 minute.",
                   "Pour in the eggs, tilt the pan and cook until almost set.",
                   "Scatter the feta over one half, fold and slide onto a plate."));

        b.add(r("Fluffy Pancakes", "Tall, golden, diner-style pancakes. A great home for milk that's nearly done.",
                VEGETARIAN, BREAKFAST, "American", EASY, 10, 15, 3, "stack:#F2C14E,#9C6644,#FF9B8E")
                .i("Flour", 150, G, GRAINS).i("Milk", 250, ML, DAIRY).i("Egg", 1, PCS, DAIRY).i("Sugar", 2, TBSP, OTHERS)
                .i("Baking powder", 2, TSP, OTHERS).i("Butter", 20, G, DAIRY).o("Strawberry", 6, PCS, FRUITS)
                .s("Whisk the flour, sugar, baking powder and a pinch of salt.",
                   "Whisk the milk, egg and melted butter, then fold into the dry ingredients. A few lumps are fine.",
                   "Cook ladlefuls on a hot greased pan until bubbles appear, then flip.",
                   "Serve stacked, with sliced strawberries on top."));

        b.add(r("Classic French Toast", "Crisp outside, custardy inside. Turns stale bread into breakfast gold.",
                VEGETARIAN, BREAKFAST, "French", EASY, 5, 10, 2, "toast:#E9C46A,#9C6644,#FF9B8E")
                .i("Bread", 4, PCS, GRAINS).i("Egg", 2, PCS, DAIRY).i("Milk", 120, ML, DAIRY).i("Butter", 15, G, DAIRY)
                .i("Cinnamon", 1, TSP, SPICES).o("Honey", 2, TBSP, OTHERS)
                .s("Whisk the eggs, milk and cinnamon in a shallow dish.",
                   "Soak each slice of bread for 10 seconds per side.",
                   "Fry in butter over medium heat for 2 to 3 minutes per side until golden.",
                   "Drizzle with honey and serve warm."));

        b.add(r("Simple Scrambled Eggs", "Soft, creamy scrambled eggs on toast in five minutes.",
                VEGETARIAN, BREAKFAST, "British", EASY, 2, 5, 1, "toast:#F9DC5C,#E9C46A,#76B041")
                .i("Egg", 2, PCS, DAIRY).i("Butter", 10, G, DAIRY).i("Bread", 1, PCS, GRAINS).o("Milk", 2, TBSP, DAIRY)
                .s("Toast the bread.", "Whisk the eggs with the milk and a pinch of salt.",
                   "Melt the butter over low heat, add the eggs and stir slowly until just set.", "Pile onto the toast."));

        b.add(r("Garlic Broccoli Stir-Fry", "Bright, crunchy broccoli in a glossy garlic-soy glaze.",
                VEGAN, MAIN, "Chinese", EASY, 10, 8, 2, "pan:#76B041,#245C45,#F2C14E")
                .i("Broccoli", 300, G, VEGETABLES).i("Garlic", 3, PCS, VEGETABLES).i("Soy sauce", 2, TBSP, SPICES)
                .i("Vegetable oil", 1, TBSP, OTHERS).i("Rice", 150, G, GRAINS).o("Sesame seed", 1, TSP, SPICES)
                .s("Cook the rice.", "Cut the broccoli into small florets.",
                   "Stir-fry the broccoli in hot oil for 4 minutes, then add the sliced garlic for 1 minute.",
                   "Add the soy sauce and a splash of water, toss until glossy and serve over rice."));

        b.add(r("Simple Chicken Curry", "A mild, family-friendly curry with tender chicken in a tomato sauce.",
                GENERAL, MAIN, "Indian", MEDIUM, 15, 30, 4, "bowl:#F28F3B,#F2C14E,#FFFFFF")
                .i("Chicken breast", 500, G, MEAT_SEAFOOD).i("Onion", 1, PCS, VEGETABLES).i("Garlic", 3, PCS, VEGETABLES)
                .i("Canned tomato", 400, G, CANNED_GOODS).i("Curry powder", 2, TBSP, SPICES).i("Rice", 300, G, GRAINS)
                .i("Vegetable oil", 2, TBSP, OTHERS).o("Yogurt", 100, G, DAIRY)
                .s("Cook the rice.", "Fry the chopped onion in oil until soft, about 8 minutes. Add the garlic and curry powder for 1 minute.",
                   "Add the diced chicken and brown it all over.",
                   "Pour in the tomatoes, simmer for 20 minutes, then stir in the yogurt off the heat."));

        b.add(r("Garlic Butter Shrimp", "Juicy shrimp in sizzling garlic butter with a squeeze of lemon.",
                GENERAL, MAIN, "Mediterranean", EASY, 10, 6, 2, "plate:#FF9B8E,#F2C14E,#76B041")
                .i("Shrimp", 300, G, MEAT_SEAFOOD).i("Butter", 30, G, DAIRY).i("Garlic", 4, PCS, VEGETABLES)
                .i("Lemon juice", 2, TBSP, FRUITS).o("Parsley", 5, G, SPICES)
                .s("Pat the shrimp dry and season.", "Melt the butter, add the garlic for 30 seconds.",
                   "Add the shrimp and cook 2 minutes per side until pink.", "Finish with lemon juice and parsley."));

        b.add(r("Lemon Herb Chicken", "Golden pan-roasted chicken with lemon, garlic and potatoes.",
                GENERAL, MAIN, "Mediterranean", MEDIUM, 10, 35, 2, "plate:#F2C14E,#9C6644,#76B041")
                .i("Chicken breast", 400, G, MEAT_SEAFOOD).i("Potato", 400, G, VEGETABLES).i("Lemon juice", 3, TBSP, FRUITS)
                .i("Garlic", 3, PCS, VEGETABLES).i("Olive oil", 3, TBSP, OTHERS).i("Oregano", 1, TSP, SPICES)
                .s("Heat the oven to 200°C. Cut the potatoes into chunks.",
                   "Toss the chicken and potatoes with oil, lemon juice, garlic, oregano and salt.",
                   "Roast for 30 to 35 minutes until the chicken is cooked through and the potatoes are crisp."));

        b.add(r("Beef Tacos", "Spiced beef in warm tortillas with crunchy lettuce and tomato.",
                GENERAL, MAIN, "Mexican", EASY, 10, 15, 4, "wrap:#E9C46A,#9C6644,#76B041")
                .i("Ground beef", 500, G, MEAT_SEAFOOD).i("Tortilla", 8, PCS, GRAINS).i("Tomato", 2, PCS, VEGETABLES)
                .i("Lettuce", 1, PCS, VEGETABLES).i("Onion", 1, PCS, VEGETABLES).i("Cumin", 2, TSP, SPICES)
                .o("Cheddar", 80, G, DAIRY)
                .s("Brown the beef with the chopped onion. Drain the excess fat.",
                   "Add the cumin, salt and a splash of water; simmer 5 minutes.",
                   "Warm the tortillas and fill with beef, shredded lettuce, diced tomato and cheese."));

        b.add(r("Spaghetti Bolognese", "A rich, slow-simmered meat sauce over spaghetti.",
                GENERAL, MAIN, "Italian", MEDIUM, 15, 45, 4, "bowl:#E4572E,#9C6644,#F2C14E")
                .i("Pasta", 400, G, GRAINS).i("Ground beef", 500, G, MEAT_SEAFOOD).i("Canned tomato", 400, G, CANNED_GOODS)
                .i("Onion", 1, PCS, VEGETABLES).i("Carrot", 1, PCS, VEGETABLES).i("Garlic", 2, PCS, VEGETABLES)
                .i("Olive oil", 2, TBSP, OTHERS).o("Parmesan", 40, G, DAIRY)
                .s("Finely chop the onion, carrot and garlic; soften in oil for 10 minutes.",
                   "Add the beef and brown well.", "Add the tomatoes and simmer gently for 30 minutes.",
                   "Serve over cooked pasta with parmesan."));

        b.add(r("Chickpea Coconut Curry", "Creamy, warming and ready in 25 minutes. All from the pantry.",
                VEGAN, MAIN, "Indian", EASY, 10, 20, 4, "bowl:#F2C14E,#F28F3B,#76B041")
                .i("Chickpea", 400, G, CANNED_GOODS).i("Coconut milk", 400, ML, CANNED_GOODS).i("Onion", 1, PCS, VEGETABLES)
                .i("Garlic", 2, PCS, VEGETABLES).i("Curry powder", 2, TBSP, SPICES).i("Spinach", 100, G, VEGETABLES)
                .i("Rice", 300, G, GRAINS)
                .s("Cook the rice.", "Soften the onion, add garlic and curry powder for 1 minute.",
                   "Add the drained chickpeas and coconut milk; simmer 10 minutes.", "Stir in the spinach until wilted."));

        b.add(r("Rainbow Veggie Fried Rice", "The ultimate fridge-clearer: day-old rice and whatever veg you have.",
                VEGETARIAN, MAIN, "Chinese", EASY, 10, 10, 2, "bowl:#F2C14E,#E4572E,#76B041")
                .i("Rice", 200, G, GRAINS).i("Egg", 2, PCS, DAIRY).i("Carrot", 1, PCS, VEGETABLES)
                .i("Bell pepper", 1, PCS, VEGETABLES).i("Peas", 80, G, VEGETABLES).i("Soy sauce", 2, TBSP, SPICES)
                .i("Vegetable oil", 1, TBSP, OTHERS).o("Spring onion", 2, PCS, VEGETABLES)
                .s("Cook and cool the rice (or use leftovers).", "Scramble the eggs in oil and set aside.",
                   "Stir-fry the diced carrot and pepper for 3 minutes, add the peas and rice.",
                   "Add the soy sauce and eggs, toss and finish with spring onion."));

        b.add(r("Greek Salad", "Crunchy, juicy and bright. No cooking required.",
                VEGETARIAN, SALAD, "Greek", EASY, 10, 0, 2, "salad:#76B041,#E4572E,#FFFFFF")
                .i("Tomato", 3, PCS, VEGETABLES).i("Cucumber", 1, PCS, VEGETABLES).i("Feta", 100, G, DAIRY)
                .i("Red onion", 0.5, PCS, VEGETABLES).i("Olive oil", 2, TBSP, OTHERS).o("Olive", 50, G, CANNED_GOODS)
                .s("Chop the tomatoes and cucumber into chunks; slice the onion thinly.",
                   "Top with the feta and olives.", "Drizzle with olive oil and season with oregano and pepper."));

        b.add(r("Avocado Toast", "Smashed avocado with lemon and chilli on crunchy toast.",
                VEGAN, BREAKFAST, "Modern", EASY, 5, 3, 1, "toast:#76B041,#E9C46A,#E4572E")
                .i("Bread", 2, PCS, GRAINS).i("Avocado", 1, PCS, FRUITS).i("Lemon juice", 1, TSP, FRUITS)
                .o("Chili flakes", 0.5, TSP, SPICES)
                .s("Toast the bread.", "Mash the avocado with lemon juice and salt.", "Spread and top with chilli flakes."));

        b.add(r("Banana Oat Smoothie", "Thick, creamy and filling. Rescue those spotty bananas!",
                VEGETARIAN, DRINK, "Modern", EASY, 5, 0, 2, "glass:#F9DC5C,#E9D8A6,#BDA7E8")
                .i("Banana", 2, PCS, FRUITS).i("Milk", 400, ML, DAIRY).i("Rolled oat", 40, G, GRAINS).o("Honey", 1, TBSP, OTHERS)
                .s("Add everything to a blender.", "Blend until smooth and serve straight away."));

        b.add(r("Banana Bread", "Moist, sweet loaf. The best thing to happen to overripe bananas.",
                VEGETARIAN, BAKING, "American", MEDIUM, 15, 55, 8, "loaf:#9C6644,#F2C14E,#E9C46A")
                .i("Banana", 3, PCS, FRUITS).i("Flour", 250, G, GRAINS).i("Sugar", 150, G, OTHERS).i("Butter", 100, G, DAIRY)
                .i("Egg", 2, PCS, DAIRY).i("Baking powder", 2, TSP, OTHERS).o("Walnut", 50, G, OTHERS)
                .s("Heat the oven to 175°C and line a loaf tin.", "Mash the bananas; beat in the melted butter, sugar and eggs.",
                   "Fold in the flour and baking powder (and walnuts).", "Bake for 50 to 55 minutes until a skewer comes out clean."));

        b.add(r("Strawberry Yogurt Parfait", "Layers of creamy yogurt, crunchy oats and sweet strawberries.",
                VEGETARIAN, BREAKFAST, "Modern", EASY, 5, 0, 2, "glass:#FF9B8E,#FFF4D6,#E9C46A")
                .i("Greek yogurt", 300, G, DAIRY).i("Strawberry", 10, PCS, FRUITS).i("Granola", 60, G, GRAINS)
                .o("Honey", 1, TBSP, OTHERS)
                .s("Slice the strawberries.", "Layer yogurt, strawberries and granola in two glasses.", "Drizzle with honey."));

        b.add(r("Lentil Soup", "Hearty, cheap and cosy red lentil soup with carrot and cumin.",
                VEGAN, SOUP, "Middle Eastern", EASY, 10, 30, 4, "pot:#F28F3B,#E9C46A,#76B041")
                .i("Red lentil", 250, G, GRAINS).i("Carrot", 2, PCS, VEGETABLES).i("Onion", 1, PCS, VEGETABLES)
                .i("Vegetable broth", 1, L, CANNED_GOODS).i("Cumin", 2, TSP, SPICES).i("Olive oil", 2, TBSP, OTHERS)
                .o("Lemon juice", 1, TBSP, FRUITS)
                .s("Soften the chopped onion and carrot in oil for 8 minutes. Add the cumin.",
                   "Add the rinsed lentils and broth; simmer for 25 minutes.",
                   "Blend partly for a creamy texture, then add the lemon juice and season."));

        b.add(r("Shakshuka", "Eggs gently poached in a spiced pepper and tomato sauce.",
                VEGETARIAN, BREAKFAST, "Middle Eastern", MEDIUM, 10, 20, 2, "pan:#E4572E,#F9DC5C,#76B041")
                .i("Egg", 4, PCS, DAIRY).i("Canned tomato", 400, G, CANNED_GOODS).i("Bell pepper", 1, PCS, VEGETABLES)
                .i("Onion", 1, PCS, VEGETABLES).i("Garlic", 2, PCS, VEGETABLES).i("Paprika", 1, TSP, SPICES)
                .i("Olive oil", 2, TBSP, OTHERS).o("Bread", 2, PCS, GRAINS)
                .s("Soften the onion and pepper in oil for 8 minutes. Add the garlic and paprika.",
                   "Add the tomatoes and simmer 8 minutes until thick.",
                   "Make four wells, crack in the eggs, cover and cook 6 minutes until the whites are set.",
                   "Serve with bread for dipping."));

        b.add(r("Mushroom Risotto", "Creamy, patiently stirred rice with golden mushrooms.",
                VEGETARIAN, MAIN, "Italian", HARD, 10, 35, 3, "bowl:#E9D8A6,#9C6644,#76B041")
                .i("Arborio rice", 250, G, GRAINS).i("Mushroom", 250, G, VEGETABLES).i("Vegetable broth", 1, L, CANNED_GOODS)
                .i("Onion", 1, PCS, VEGETABLES).i("Butter", 30, G, DAIRY).i("Parmesan", 50, G, DAIRY)
                .s("Fry the mushrooms in half the butter until golden; set aside.",
                   "Soften the onion, add the rice and toast 1 minute.",
                   "Add hot broth a ladle at a time, stirring, for about 20 minutes.",
                   "Stir in the mushrooms, remaining butter and parmesan. Rest 2 minutes."));

        b.add(r("Salmon & Greens Traybake", "One tray, crispy-edged salmon and roasted greens.",
                KETO, MAIN, "Modern", EASY, 10, 20, 2, "plate:#FF9B8E,#76B041,#F2C14E")
                .i("Salmon", 300, G, MEAT_SEAFOOD).i("Broccoli", 250, G, VEGETABLES).i("Zucchini", 1, PCS, VEGETABLES)
                .i("Olive oil", 2, TBSP, OTHERS).i("Lemon juice", 1, TBSP, FRUITS).i("Garlic", 2, PCS, VEGETABLES)
                .s("Heat the oven to 200°C.", "Toss the broccoli and sliced zucchini with oil, garlic and salt on a tray.",
                   "Add the salmon, drizzle with lemon and roast for 15 to 18 minutes."));

        b.add(r("Keto Cauliflower Mac & Cheese", "All the cheesy comfort, none of the pasta.",
                KETO, MAIN, "American", MEDIUM, 10, 25, 4, "dish:#F9DC5C,#FFFFFF,#E9C46A")
                .i("Cauliflower", 1, PCS, VEGETABLES).i("Cheddar", 150, G, DAIRY).i("Heavy cream", 150, ML, DAIRY)
                .i("Butter", 20, G, DAIRY).i("Mustard", 1, TSP, SPICES)
                .s("Heat the oven to 190°C. Boil the cauliflower florets for 5 minutes and drain well.",
                   "Melt the butter, add the cream and mustard, then stir in most of the cheddar until smooth.",
                   "Mix with the cauliflower, top with the remaining cheese and bake for 20 minutes."));

        b.add(r("Zucchini Noodle Stir-Fry", "Light, low-carb noodles with chicken and crunchy veg.",
                KETO, MAIN, "Asian", EASY, 15, 10, 2, "bowl:#76B041,#F2C14E,#FF9B8E")
                .i("Zucchini", 2, PCS, VEGETABLES).i("Chicken breast", 250, G, MEAT_SEAFOOD).i("Bell pepper", 1, PCS, VEGETABLES)
                .i("Tamari", 2, TBSP, SPICES).i("Garlic", 2, PCS, VEGETABLES).i("Olive oil", 1, TBSP, OTHERS)
                .s("Spiralise the zucchini.", "Stir-fry the sliced chicken in oil until cooked; add the pepper and garlic.",
                   "Add the zucchini noodles and tamari; toss for 2 minutes only."));

        b.add(r("Keto Egg Muffins", "Grab-and-go breakfast bites packed with veg and cheese.",
                KETO, BREAKFAST, "Modern", EASY, 10, 20, 6, "muffin:#F9DC5C,#76B041,#E4572E")
                .i("Egg", 6, PCS, DAIRY).i("Spinach", 60, G, VEGETABLES).i("Bell pepper", 1, PCS, VEGETABLES)
                .i("Cheddar", 60, G, DAIRY).o("Bacon", 3, PCS, MEAT_SEAFOOD)
                .s("Heat the oven to 180°C and grease a 6-hole muffin tin.",
                   "Whisk the eggs; stir in the chopped spinach, pepper and cheese.",
                   "Divide between the holes and bake 18 to 20 minutes until set."));

        b.add(r("Hummus Veggie Wrap", "A crunchy, colourful lunch you can make in five minutes.",
                VEGAN, MAIN, "Middle Eastern", EASY, 8, 0, 1, "wrap:#E9C46A,#76B041,#E4572E")
                .i("Tortilla", 1, PCS, GRAINS).i("Hummus", 60, G, OTHERS).i("Carrot", 1, PCS, VEGETABLES)
                .i("Cucumber", 0.5, PCS, VEGETABLES).i("Spinach", 30, G, VEGETABLES)
                .s("Spread the hummus over the tortilla.", "Add the grated carrot, sliced cucumber and spinach.",
                   "Roll up tightly and slice in half."));

        b.add(r("Black Bean Quesadillas", "Melty, crispy, golden and ready in 15 minutes.",
                VEGETARIAN, MAIN, "Mexican", EASY, 5, 10, 2, "wrap:#F2C14E,#245C45,#E4572E")
                .i("Tortilla", 4, PCS, GRAINS).i("Black bean", 200, G, CANNED_GOODS).i("Cheddar", 100, G, DAIRY)
                .i("Bell pepper", 1, PCS, VEGETABLES).i("Cumin", 1, TSP, SPICES)
                .s("Mash the beans roughly with the cumin.", "Spread over two tortillas; add the diced pepper and cheese.",
                   "Top with the remaining tortillas and cook 3 minutes per side in a dry pan."));

        b.add(r("Potato & Pea Samosa Bake", "All the flavour of samosas, baked in a dish.",
                VEGAN, MAIN, "Indian", MEDIUM, 15, 30, 4, "dish:#F2C14E,#76B041,#F28F3B")
                .i("Potato", 600, G, VEGETABLES).i("Peas", 150, G, VEGETABLES).i("Onion", 1, PCS, VEGETABLES)
                .i("Curry powder", 1, TBSP, SPICES).i("Vegetable oil", 2, TBSP, OTHERS).i("Tortilla", 2, PCS, GRAINS)
                .s("Boil the diced potatoes for 10 minutes and drain.", "Fry the onion with the curry powder; mix in the potatoes and peas.",
                   "Spread in a dish, top with torn tortilla brushed with oil and bake at 200°C for 15 minutes."));

        b.add(r("Apple Cinnamon Oatmeal", "Warm, creamy oats with soft apple and cinnamon.",
                VEGETARIAN, BREAKFAST, "Modern", EASY, 5, 8, 2, "bowl:#E9D8A6,#E4572E,#9C6644")
                .i("Rolled oat", 100, G, GRAINS).i("Milk", 400, ML, DAIRY).i("Apple", 1, PCS, FRUITS)
                .i("Cinnamon", 1, TSP, SPICES).o("Honey", 1, TBSP, OTHERS)
                .s("Simmer the oats in milk for 5 minutes, stirring.", "Grate in the apple and add the cinnamon.",
                   "Cook 2 more minutes and serve with honey."));

        b.add(r("Carrot Ginger Soup", "Sunny, smooth soup with a gentle ginger kick.",
                VEGAN, SOUP, "Modern", EASY, 10, 25, 4, "pot:#F28F3B,#F2C14E,#76B041")
                .i("Carrot", 6, PCS, VEGETABLES).i("Onion", 1, PCS, VEGETABLES).i("Ginger", 20, G, SPICES)
                .i("Vegetable broth", 750, ML, CANNED_GOODS).i("Olive oil", 1, TBSP, OTHERS).o("Coconut milk", 100, ML, CANNED_GOODS)
                .s("Soften the onion and ginger in oil.", "Add the sliced carrots and broth; simmer 20 minutes.",
                   "Blend until smooth and swirl through coconut milk."));

        b.add(r("Caprese Salad", "Ripe tomatoes, creamy mozzarella and fragrant basil.",
                VEGETARIAN, SALAD, "Italian", EASY, 5, 0, 2, "salad:#E4572E,#FFFFFF,#76B041")
                .i("Tomato", 3, PCS, VEGETABLES).i("Mozzarella", 125, G, DAIRY).i("Basil", 10, G, SPICES)
                .i("Olive oil", 1, TBSP, OTHERS)
                .s("Slice the tomatoes and mozzarella.", "Arrange alternately with basil leaves.",
                   "Drizzle with olive oil and season with salt and pepper."));

        b.add(r("Chocolate Mug Cake", "A warm chocolate cake for one, in 90 seconds.",
                VEGETARIAN, DESSERT, "Modern", EASY, 3, 2, 1, "muffin:#6B4226,#9C6644,#FF9B8E")
                .i("Flour", 30, G, GRAINS).i("Sugar", 2, TBSP, OTHERS).i("Cocoa powder", 1, TBSP, OTHERS)
                .i("Milk", 3, TBSP, DAIRY).i("Butter", 15, G, DAIRY).i("Egg", 1, PCS, DAIRY)
                .s("Melt the butter in a large mug.", "Whisk in everything else until smooth.",
                   "Microwave for 70 to 90 seconds until risen. Eat warm!"));

        b.add(r("Peanut Noodle Salad", "Cold noodles in a punchy peanut-lime dressing.",
                VEGAN, SALAD, "Asian", EASY, 15, 5, 2, "bowl:#E9C46A,#76B041,#FF9B8E")
                .i("Noodle", 150, G, GRAINS).i("Peanut butter", 3, TBSP, OTHERS).i("Soy sauce", 2, TBSP, SPICES)
                .i("Lime juice", 1, TBSP, FRUITS).i("Carrot", 1, PCS, VEGETABLES).i("Cucumber", 0.5, PCS, VEGETABLES)
                .s("Cook, drain and rinse the noodles in cold water.", "Whisk the peanut butter, soy sauce, lime and a splash of water.",
                   "Toss with the noodles and the julienned carrot and cucumber."));

        b.add(r("Tuna Pasta Bake", "Creamy, cheesy store-cupboard bake.",
                GENERAL, MAIN, "British", EASY, 10, 25, 4, "dish:#F9DC5C,#E4572E,#FFFFFF")
                .i("Pasta", 300, G, GRAINS).i("Canned tuna", 300, G, CANNED_GOODS).i("Canned tomato", 400, G, CANNED_GOODS)
                .i("Cheddar", 100, G, DAIRY).i("Onion", 1, PCS, VEGETABLES).o("Sweetcorn", 150, G, CANNED_GOODS)
                .s("Heat the oven to 200°C and cook the pasta.", "Soften the onion, add the tomatoes and simmer 5 minutes.",
                   "Mix with the pasta, tuna and sweetcorn, top with cheese and bake 15 minutes."));

        b.add(r("Berry Chia Pudding", "Overnight breakfast that tastes like dessert.",
                VEGAN, BREAKFAST, "Modern", EASY, 5, 0, 2, "glass:#BDA7E8,#FF9B8E,#FFF4D6")
                .i("Chia seed", 40, G, GRAINS).i("Almond milk", 300, ML, DAIRY).i("Berry", 100, G, FRUITS)
                .i("Maple syrup", 1, TBSP, OTHERS)
                .s("Stir the chia seeds into the almond milk and maple syrup.", "Chill overnight (or at least 2 hours).",
                   "Top with berries."));

        return b.stream().map(x -> x.r).toList();
    }
}
