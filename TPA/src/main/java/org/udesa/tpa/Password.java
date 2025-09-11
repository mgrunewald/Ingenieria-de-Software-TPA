package org.udesa.tpa;

import java.util.Objects;

public record Password(String contrasena) {
    public Password {
        Objects.requireNonNull(contrasena, "Password can't be null");
        ensure(!contrasena.isBlank(), "Password can't be blank");
        ensure(contrasena.length() >= 8, "Password is short");
    }

    private static void ensure(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}