package org.udesa.tpa;
import java.util.Objects;

public record Username(String usuario) {
    public Username {
        Objects.requireNonNull(usuario, "Username can't be null");
        if (usuario.isBlank()) {
            throw new IllegalArgumentException("Username can't be blank");
        }
    }
}

