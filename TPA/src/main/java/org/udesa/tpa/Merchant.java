package org.udesa.tpa;

public record Merchant(String id, String secret) {
    public Merchant {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("merchant id inválido");
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("merchant secret inválido");
        }
    }
}
