package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    @Test
    void test01CreatesGiftCardCorrectlyWithOwnerAndInitialBalance() {
        GiftCard card = new GiftCard("martina", 1000);
        assertEquals("martina", card.owner());
        assertEquals(1000, card.balance());
    }

    @Test
    void test02InitialBalanceIsNotNegative() {
        GiftCard card = new GiftCard("martina", 1000);
        assertFalse(card.balance() < 0);
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", -3));
    }

    @Test
    void test03ChargesCorrectly() {
        GiftCard card = new GiftCard("martina", 1000);
        card.charge(500, "kiosco");
        assertEquals(500, card.balance());
    }

    @Test
    void test04CantChargeMoreThanBalance() {
        GiftCard card = new GiftCard("martina", 1000);
        assertThrows(IllegalArgumentException.class, () -> card.charge(2000, "kiosco"));
    }

    @Test
    void test05CanNotAddANegativeBalance() {
        GiftCard card = new GiftCard("martina", 1000);
        assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500));
    }

    @Test
    void test06AddsBalanceCorrectly() {
        GiftCard card = new GiftCard("martina", 1000);
        card.addBalance(1000);
        assertEquals(2000, card.balance());
    }

}
