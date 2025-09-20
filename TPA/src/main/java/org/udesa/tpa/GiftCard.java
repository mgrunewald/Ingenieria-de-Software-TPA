package org.udesa.tpa;

import java.util.Optional;
import static org.udesa.tpa.Utils.*;

public class GiftCard {
    private final String owner;
    private final String cardNumber;
    private int balance;

    public static String VALUE_MUST_BE_POSITIVE = "Amount must be a positive number";
    public static String NEGATIVE_INITIAL_BALANCE = "Initial Balance must be a positive number";
    public static String INSUFFICIENT_FUNDS = "Initial Balance must be a positive number";


    public GiftCard(String owner, String cardNumber, int initialBalance) {
        this.owner = nonBlank(owner, "owner");
        this.cardNumber = nonBlank(cardNumber, "cardNumber");
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
        nonBlank(description, "description");

        balance = Optional.of(balance - amount)
                .filter(v -> v >= 0)
                .orElseThrow(() -> new IllegalArgumentException(INSUFFICIENT_FUNDS));
    }

    public String owner() { return owner; }
    public String cardNumber() { return cardNumber; }
    public int balance() { return balance; }
}
