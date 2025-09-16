package org.udesa.tpa;

import java.time.Instant;

import static org.springframework.util.Assert.*;

public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    public Charge {
        hasText(cardNumber, "cardNumber inválido");
        isTrue(cardNumber.matches("\\d+"), "cardNumber debe ser numérico positivo");
        hasText(merchantId, "merchantId inválido");
        isTrue(amount > 0, "amount debe ser > 0");
        hasText(description, "description inválida");
        notNull(timestamp, "timestamp no puede ser null");
    }
}
