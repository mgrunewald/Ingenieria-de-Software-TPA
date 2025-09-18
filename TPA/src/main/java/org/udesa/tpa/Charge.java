// src/main/java/org/udesa/tpa/Charge.java
package org.udesa.tpa;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Representa un cargo realizado sobre una gift card.
 * Invariantes de dominio (sin ifs):
 * - cardNumber: no vacío y compuesto solo por dígitos.
 * - merchantId: no vacío.
 * - amount: > 0 (en unidades mínimas, p.ej. centavos).
 * - description: no vacía.
 * - timestamp: no nulo.
 *
 * Las validaciones usan Optional.filter().orElseThrow() para cumplir
 * con la restricción de evitar condicionales explícitos en negocio.
 */
public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    public Charge {
        cardNumber = nonBlank(cardNumber, "cardNumber");
        merchantId = nonBlank(merchantId, "merchantId");
        description = nonBlank(description, "description");

        // cardNumber solo dígitos
        ensure(DIGITS.matcher(cardNumber).matches(), "cardNumber debe ser numérico positivo");
        // amount > 0
        ensure(amount > 0, "amount debe ser > 0");
        // timestamp no nulo
        timestamp = Optional.ofNullable(timestamp)
                .orElseThrow(() -> new IllegalArgumentException("timestamp no puede ser null"));
    }

    /** Valida que un string no sea nulo ni en blanco. */
    private static String nonBlank(String value, String field) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(field + " inválido"));
    }

    /** Asegura una condición sin ifs: true → ok; false → IllegalArgumentException. */
    private static void ensure(boolean condition, String message) {
        Optional.of(condition)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }
}
