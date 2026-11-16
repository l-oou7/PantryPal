package com.pantrypal.engine;

import java.util.Map;

/**
 * Normalises free-text ingredient names so "Tomatoes", " tomato " and "Roma tomatoes"
 * all compare equal to "tomato".
 */
public final class IngredientNames {

    /** Synonyms and common variants mapped to one canonical name. */
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("roma tomato", "tomato"),
            Map.entry("cherry tomato", "tomato"),
            Map.entry("spaghetti", "pasta"),
            Map.entry("penne", "pasta"),
            Map.entry("fusilli", "pasta"),
            Map.entry("macaroni", "pasta"),
            Map.entry("scallion", "spring onion"),
            Map.entry("green onion", "spring onion"),
            Map.entry("yellow onion", "onion"),
            Map.entry("white onion", "onion"),
            Map.entry("brown onion", "onion"),
            Map.entry("garlic clove", "garlic"),
            Map.entry("extra virgin olive oil", "olive oil"),
            Map.entry("all purpose flour", "flour"),
            Map.entry("plain flour", "flour"),
            Map.entry("white sugar", "sugar"),
            Map.entry("granulated sugar", "sugar"),
            Map.entry("caster sugar", "sugar"),
            Map.entry("whole milk", "milk"),
            Map.entry("full fat milk", "milk"),
            Map.entry("large egg", "egg"),
            Map.entry("free range egg", "egg"),
            Map.entry("chicken breast fillet", "chicken breast"),
            Map.entry("minced beef", "ground beef"),
            Map.entry("beef mince", "ground beef"),
            Map.entry("bell pepper", "bell pepper"),
            Map.entry("capsicum", "bell pepper"),
            Map.entry("courgette", "zucchini"),
            Map.entry("aubergine", "eggplant"),
            Map.entry("coriander", "cilantro"),
            Map.entry("garbanzo", "chickpea"),
            Map.entry("garbanzo bean", "chickpea"),
            Map.entry("lemon", "lemon"),
            Map.entry("fresh lemon juice", "lemon juice"),
            Map.entry("parmesan cheese", "parmesan"),
            Map.entry("cheddar cheese", "cheddar"),
            Map.entry("mozzarella cheese", "mozzarella"),
            Map.entry("greek yoghurt", "greek yogurt"),
            Map.entry("yoghurt", "yogurt"),
            Map.entry("natural yogurt", "yogurt"),
            Map.entry("plain yogurt", "yogurt"));

    private IngredientNames() {}

    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (s.isEmpty()) return s;
        // singularise the last word only ("cherry tomatoes" -> "cherry tomato")
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace >= 0 ? s.substring(0, lastSpace + 1) : "";
        String last = lastSpace >= 0 ? s.substring(lastSpace + 1) : s;
        s = head + singular(last);
        return ALIASES.getOrDefault(s, s);
    }

    static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";   // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);                          // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes"))
            return w.substring(0, w.length() - 2);                                                  // peaches -> peach
        if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) return w;                // hummus, swiss
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);                             // carrots -> carrot
        return w;
    }

    /** "chicken breast" -> "Chicken breast" for display. */
    public static String display(String name) {
        if (name == null || name.isBlank()) return "";
        String t = name.trim();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
