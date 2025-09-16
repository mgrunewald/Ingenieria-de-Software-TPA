package org.udesa.tpa;

import java.time.Instant;

public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    public Charge {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("cardNumber inválido");
        }
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId inválido");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount debe ser > 0");
        }
        if (description == null) {
            throw new IllegalArgumentException("description no puede ser null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp no puede ser null");
        }
    }
}
 //SACAR TODOS LOS IFS DESPUES