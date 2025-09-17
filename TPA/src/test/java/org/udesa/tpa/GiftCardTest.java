package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    private GiftCard card;

    @BeforeEach
    public void createCard() {
        this.card = new GiftCard("martina", "1", 1000);
    }

    @Test
    void test01CreatesGiftCardCorrectlyWithOwnerAndInitialBalance() {
        assertEquals("martina", card.owner());
        assertEquals(1000, card.balance());
    }

    @Test
    void test02InitialBalanceIsNotNegative() {
        assertFalse(card.balance() < 0);
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "1", -3));
    }

    @Test
    void test03ChargesCorrectly() {
        card.charge(500, "kiosco");
        assertEquals(500, card.balance());
    }

    @Test
    void test04CantChargeMoreThanBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(2000, "kiosco"));
    }

    @Test
    void test05CanNotChargeANegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(-500, "kiosco"));
    }

    @Test
    void test06CanNotAddANegativeBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500));
    }

    @Test
    void test07AddsBalanceCorrectly() {
        card.addBalance(1000);
        assertEquals(2000, card.balance());
    }

    @Test
    void test08Creates2GiftCardCorrectly() {
        GiftCard card1 = new GiftCard("martina", "1", 1000);
        GiftCard card2 = new GiftCard("martina", "2", 100);
        assertEquals(1000, card1.balance());
        assertEquals(100, card2.balance());
    }

    @Test
    void test09RisesErrorsWhenMakingTransactionsWithNoMoney() {
        assertThrows(IllegalArgumentException.class, () -> card.addBalance(0));
        assertThrows(IllegalArgumentException.class, () -> card.charge(0, "nada"));
    }

    @Test
    void test10FailsToChargeWithInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(0, ""));
    }

    @Test
    void test11FailsToCreateGiftCardWithInvalidOwnerAndCardNumber() {
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(null, "1", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("  ", "1", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", null, 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "  ", 100));
    }

}
