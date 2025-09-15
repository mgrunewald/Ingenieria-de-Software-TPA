package org.udesa.tpa;

import static org.springframework.util.Assert.*;


public class GiftCard {
    private final String owner;
    private int balance;

    public  GiftCard(String owner,  int initialBalance) {
        isTrue(owner != null && !owner.isBlank(), "Owner inválido");
        isTrue(initialBalance >= 0, "Balance inicial negativo");
        this.owner = owner;
        this.balance = initialBalance;
    }

    public String owner() { return owner; }
    public int balance()  { return balance; }

    public void addBalance(int amount) {
        isTrue(amount > 0, "Monto a agregar debe ser positivo");
        balance += amount;
    }

    public void charge(int amount, String description) {
        isTrue(amount > 0, "Monto a cobrer debe ser positivo");
        isTrue(balance >= amount, "Fondos insuficientes");
        balance -= amount;
    }

}
