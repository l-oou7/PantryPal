package com.pantrypal.domain;

import java.util.Collection;
import java.util.regex.Pattern;

/** Whole-word keyword matching, so that "eggplant" is not "egg" and "goat cheese" is not "oat". */
public final class TextMatch {

    private TextMatch() {}

    public static boolean containsWord(String text, String keyword) {
        if (text == null || keyword == null || keyword.isBlank()) return false;
        String regex = "(^|[^a-z])" + Pattern.quote(keyword) + "(s|es)?($|[^a-z])";
        return Pattern.compile(regex).matcher(text).find();
    }

    public static boolean containsAnyWord(String text, Collection<String> keywords) {
        for (String k : keywords) {
            if (containsWord(text, k)) return true;
        }
        return false;
    }
}
