package org.udesa.tpa;

import java.util.Objects;

public record Token(String value) {
    public Token {
        requireNonBlank(value, "Token can't be null nor blank");
        ensure(value.length() == 10, "Token must have at least 10 characters");
    }

    private static void requireNonBlank(String s, String msg) {
        Objects.requireNonNull(s, msg);
        if (s.isBlank()) throw new IllegalArgumentException(msg);
    }
    private static void ensure(boolean ok, String msg) {
        if (!ok) throw new IllegalArgumentException(msg);
    }
}
