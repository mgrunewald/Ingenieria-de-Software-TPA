package org.udesa.tpa;

import java.util.Optional;
import static org.udesa.tpa.Utils.*;
import static org.udesa.tpa.Facade.*;

public class GiftCard {
    private final String owner;
    private final String cardNumber;
    private int balance;

    public static String VALUE_MUST_BE_POSITIVE = "Amount must be a positive number";
    public static String NEGATIVE_INITIAL_BALANCE = "Initial Balance must be a positive number";
    public static String INSUFFICIENT_FUNDS = "Initial Balance must be a positive number";


    public GiftCard(String owner, String cardNumber, int initialBalance) {
        this.owner = nonBlank(owner, NULL_OR_EMPTY_VALUE);
        this.cardNumber = nonBlank(cardNumber, NULL_OR_EMPTY_VALUE);
        this.balance = Optional.of(initialBalance)
                .filter(v -> v >= 0)
                .orElseThrow(() -> new IllegalArgumentException(NEGATIVE_INITIAL_BALANCE));
    }

    public void addBalance(int amount) {
        ensure(amount > 0, VALUE_MUST_BE_POSITIVE);
        balance += amount;
    }

    public void charge(int amount, String description) {
        ensure(amount > 0, VALUE_MUST_BE_POSITIVE);
        nonBlank(description, NULL_OR_EMPTY_VALUE);

        balance = Optional.of(balance - amount)
                .filter(v -> v >= 0)
                .orElseThrow(() -> new IllegalArgumentException(INSUFFICIENT_FUNDS));
    }

    public String owner() { return owner; }
    public String cardNumber() { return cardNumber; }
    public int balance() { return balance; }
}
