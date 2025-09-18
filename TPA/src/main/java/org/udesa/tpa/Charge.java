package org.udesa.tpa;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    public static String VALUE_GREATER_THAN_ZERO = "Value must be grater than 0";
    public static String VALUE_MUST_BE_NUMERIC = "Value must be numeric";
    public static String VALUE_CAN_NOT_BE_NULL = "Value must be numeric";

    public Charge {
        cardNumber = Utils.nonBlank(cardNumber, "cardNumber");
        merchantId = Utils.nonBlank(merchantId, "merchantId");
        description = Utils.nonBlank(description, "description");

        ensure(DIGITS.matcher(cardNumber).matches(), VALUE_MUST_BE_NUMERIC + VALUE_GREATER_THAN_ZERO);
        ensure(amount > 0, VALUE_GREATER_THAN_ZERO);
        timestamp = Optional.ofNullable(timestamp)
                .orElseThrow(() -> new IllegalArgumentException(VALUE_CAN_NOT_BE_NULL));
    }

    private static void ensure(boolean condition, String message) {
        Optional.of(condition)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }
}
