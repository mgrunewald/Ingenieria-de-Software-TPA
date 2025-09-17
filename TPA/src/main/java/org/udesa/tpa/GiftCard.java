package org.udesa.tpa;

import static org.springframework.util.Assert.*;


public class GiftCard {
    private final String owner;
    private final String cardNumber;
    private int balance;

    public  GiftCard(String owner, String cardNumber, int initialBalance) {
        hasText(cardNumber, "Número de tarjeta inválido");
        hasText(owner, "Owner inválido");
        isTrue(initialBalance >= 0, "Balance inicial negativo");
        this.owner = owner;
        this.cardNumber = cardNumber;
        this.balance = initialBalance;
    }

    public void addBalance(int amount) {
        isTrue(amount > 0, "Monto a agregar debe ser positivo");
        balance += amount;
    }

    public void charge(int amount, String description) {
        isTrue(amount > 0, "Monto a cobrer debe ser positivo");
        hasText(description, "Descripción vacía / inválida");
        isTrue(balance >= amount, "Fondos insuficientes");
        balance -= amount;
    }

    public String owner() { return owner; }
    public String cardNumber() { return cardNumber; }
    public int balance()  { return balance; }

}
