package org.udesa.tpa;

import java.util.Objects;

public record Password(String contrasena) {
    public Password {
        Objects.requireNonNull(contrasena, "Password can't be null");
        if (contrasena.isBlank()) {
            throw new IllegalArgumentException("Password can't be blank");
        }
        if (contrasena.length() < 8) {
            throw new IllegalArgumentException("Password is short");
        }
    }
}