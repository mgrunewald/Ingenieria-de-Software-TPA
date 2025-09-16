package org.udesa.tpa;

import static org.springframework.util.Assert.hasText;

public record Merchant(String id, String privateCredential) {
    public Merchant {
        hasText(id, "id del comercio inválido");
        hasText(privateCredential, "credencial privada del comercio inválida");
    }
}