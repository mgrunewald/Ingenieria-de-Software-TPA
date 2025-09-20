package org.udesa.tpa;

public final class Utils {

    private Utils() {
    }

    public static String nonBlank(String value, String message) {
        if (value == null) { throw new IllegalArgumentException(message); }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) { throw new IllegalArgumentException(message); }
        return trimmed;
    }

    public static void ensure(boolean condition, String message) {
        if (!condition) { throw new IllegalArgumentException(message); }
    }
}
