package net.fameless.core.util;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public final class StringUtil {

    private StringUtil() {
    }

    @NotNull public static <T extends Collection<? super String>> T copyPartialMatches(
            @NotNull final String token,
            @NotNull final Iterable<String> originals,
            @NotNull final T collection
    ) throws UnsupportedOperationException, IllegalArgumentException {
        if (token.isEmpty()) {
            for (String string : originals) {
                collection.add(string);
            }
            return collection;
        }
        for (String string : originals) {
            if (startsWithIgnoreCase(string, token)) {
                collection.add(string);
            }
        }
        return collection;
    }

    public static boolean containsChar(@NonNull String s, char c) {
        for (char ch : s.toCharArray()) {
            if (ch == c) return true;
        }
        return false;
    }

    public static boolean startsWithIgnoreCase(@NotNull final String string, @NotNull final String prefix) throws IllegalArgumentException, NullPointerException {
        if (string.length() < prefix.length()) {
            return false;
        }
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

}
