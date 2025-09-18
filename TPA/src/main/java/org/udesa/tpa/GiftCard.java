package org.udesa.tpa;

import java.util.Optional;

/**
 * Representa una Gift Card con dueño, número y balance.
 * Invariantes (sin ifs en la lógica de negocio):
 * - owner y cardNumber: no nulos ni en blanco.
 * - balance inicial: >= 0.
 * - addBalance(amount): amount > 0.
 * - charge(amount, description): amount > 0, description no en blanco,
 *   y balance suficiente (balance - amount >= 0).
 *
 * Las validaciones se expresan con Optional.filter().orElseThrow()
 * para cumplir con la restricción de evitar condicionales explícitos.
 */
public class GiftCard {
    private final String owner;
    private final String cardNumber;
    private int balance;

    public GiftCard(String owner, String cardNumber, int initialBalance) {
        this.owner = nonBlank(owner, "Owner inválido");
        this.cardNumber = nonBlank(cardNumber, "Número de tarjeta inválido");
        this.balance = Optional.of(initialBalance)
                .filter(v -> v >= 0)
                .orElseThrow(() -> new IllegalArgumentException("Balance inicial negativo"));
    }

    /** Agrega saldo; amount debe ser > 0. */
    public void addBalance(int amount) {
        ensure(amount > 0, "Monto a agregar debe ser positivo");
        balance += amount;
    }

    /** Realiza un cargo; amount > 0, descripción no vacía, y saldo suficiente. */
    public void charge(int amount, String description) {
        ensure(amount > 0, "Monto a cobrar debe ser positivo");
        nonBlank(description, "Descripción vacía / inválida");

        // Validar fondos suficientes sin if: calcular nuevo saldo y filtrar >= 0
        int newBalance = Optional.of(balance - amount)
                .filter(v -> v >= 0)
                .orElseThrow(() -> new IllegalArgumentException("Fondos insuficientes"));

        balance = newBalance;
    }

    public String owner() { return owner; }
    public String cardNumber() { return cardNumber; }
    public int balance() { return balance; }

    /** Valida string no nulo ni en blanco. */
    private static String nonBlank(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    /** Asegura condición booleana sin if: si false → IllegalArgumentException. */
    private static void ensure(boolean condition, String message) {
        Optional.of(condition)
                .filter(Boolean::booleanValue)
                .orElseThrow(() -> new IllegalArgumentException(message));
    }
}
