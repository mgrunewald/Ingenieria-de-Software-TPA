package org.udesa.tpa;

import static org.udesa.tpa.Utils.*;
import static org.udesa.tpa.Facade.*;
import static org.udesa.tpa.Charge.*;

public class GiftCard {
    private final String owner;
    private final String cardNumber;
    private int balance;

    public static String NEGATIVE_INITIAL_BALANCE = "Initial Balance must be a non-negative number";
    public static String INSUFFICIENT_FUNDS = "Charging amount must be less than the card's balance";


    public GiftCard(String owner, String cardNumber, int initialBalance) {
        this.owner = nonBlank(owner, NULL_OR_EMPTY_VALUE);
        this.cardNumber = nonBlank(cardNumber, NULL_OR_EMPTY_VALUE);
        if (initialBalance < 0) { throw new IllegalArgumentException(NEGATIVE_INITIAL_BALANCE); }
        this.balance = initialBalance;
    }

    public void addBalance(int amount) {
        ensure(amount > 0, INVALID_AMOUNT);
        balance += amount;
    }

    public void charge(int amount, String description) {
        ensure(amount > 0, INVALID_AMOUNT);
        nonBlank(description, NULL_OR_EMPTY_VALUE);
        int newBalance = balance - amount;
        if (newBalance < 0) {throw new IllegalArgumentException(INSUFFICIENT_FUNDS);}
        balance = newBalance;
    }

    public String owner() { return owner; }
    public String cardNumber() { return cardNumber; }
    public int balance() { return balance; }
}
