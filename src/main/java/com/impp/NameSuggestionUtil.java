package com.impp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class NameSuggestionUtil {
    private NameSuggestionUtil() {
    }

    public static List<String> suggest(Set<String> names, String tokenPrefix, Set<String> alreadyUsed) {
        String cleanPrefix = normalize(tokenPrefix).replace(":", "");
        Set<String> safeAlreadyUsed = alreadyUsed == null ? Set.of() : alreadyUsed;
        List<String> matches = new ArrayList<>();
        for (String name : names) {
            if (containsIgnoreCase(safeAlreadyUsed, name)) {
                continue;
            }
            if (name.toLowerCase(Locale.ROOT).startsWith(cleanPrefix.toLowerCase(Locale.ROOT))) {
                matches.add(name);
            }
        }
        matches.sort(String.CASE_INSENSITIVE_ORDER);
        return matches;
    }

    public static List<String> suggestWithColon(Set<String> names, String tokenPrefix, Set<String> alreadyUsed) {
        List<String> base = suggest(names, tokenPrefix, alreadyUsed);
        List<String> withColon = new ArrayList<>(base.size());
        for (String name : base) {
            withColon.add(name + ":");
        }
        return withColon;
    }

    private static boolean containsIgnoreCase(Set<String> names, String needle) {
        for (String name : names) {
            if (name.equalsIgnoreCase(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
