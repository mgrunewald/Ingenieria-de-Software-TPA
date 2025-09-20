package org.udesa.tpa;

import java.time.Instant;
import java.util.regex.Pattern;
import static org.udesa.tpa.Utils.*;
import static org.udesa.tpa.Facade.*;

public record Charge(
        String cardNumber,
        String merchantId,
        int amount,
        String description,
        Instant timestamp
) {
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    public static String INVALID_AMOUNT = "The charging amount must be grater than 0";
    public static String CARD_NUMBER_MUST_BE_A_NUMERIC_STRING = "Value must have only numeric character, minus symbol is also excluded";

    public Charge {
        cardNumber = nonBlank(cardNumber, NULL_OR_EMPTY_VALUE);
        merchantId = nonBlank(merchantId, NULL_OR_EMPTY_VALUE);
        description = nonBlank(description, NULL_OR_EMPTY_VALUE);

        ensure(DIGITS.matcher(cardNumber).matches(), CARD_NUMBER_MUST_BE_A_NUMERIC_STRING);
        ensure(amount > 0, INVALID_AMOUNT);
        ensure(timestamp != null, NULL_OBJECT);
    }
}
