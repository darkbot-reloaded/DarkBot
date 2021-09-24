package com.github.manolo8.darkbot.utils;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents a search over text
 */
public class StringQuery {
    private boolean matchCase = false; // If it should match case
    private boolean wordStart = true; // If it should match word start
    private boolean words = true; // If the search should split and search each word individually
    public String query;

    public boolean matches(final String toStr) {
        if (toStr == null) return false;
        if (query == null || query.isEmpty()) return true;
        if (words && query.contains(" "))
            return Arrays.stream(query.split(" ")).allMatch(q -> matches(toStr, q));
        else return matches(toStr, query);
    }

    private boolean matches(String toStr, String query) {
        if (query == null || query.isEmpty()) return true;
        if (!matchCase) {
            toStr = toStr.toLowerCase(Locale.ROOT);
            query = query.toLowerCase(Locale.ROOT);
        }
        if (!wordStart) return toStr.contains(query);
        return toStr.startsWith(query) || toStr.replace("_", " ").contains(" " + query);
    }

}
