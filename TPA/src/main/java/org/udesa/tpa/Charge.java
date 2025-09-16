package org.udesa.tpa;

import java.time.Instant;
import static org.springframework.util.Assert.hasText;

public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    public Charge {
        hasText(cardNumber,"cardNumber invalido");
        hasText(merchantId,"merchantId invalido");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount debe ser > 0");
        }
        hasText(description,"description invalido");
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp no puede ser null");
        }
    }
}
 //SACAR TODOS LOS IFS DESPUES