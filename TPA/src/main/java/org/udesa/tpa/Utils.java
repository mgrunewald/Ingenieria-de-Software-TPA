package org.udesa.tpa;

import java.util.Optional;

public final class Utils {

    private Utils() {
    }

    public static String nonBlank(String value, String field) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(field + " is null or empty"));
    }

    public static void ensure(boolean condition, String message) {
        Optional.of(condition)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }
}
