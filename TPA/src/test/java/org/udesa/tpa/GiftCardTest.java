package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {

    @Test
    void test01CreatesGiftCardCorrectlyWithOwnerAndInitialBalance() {
        GiftCard card = new GiftCard("martina", "1",1000);
        assertEquals("martina", card.owner());
        assertEquals(1000, card.balance());
    }

    @Test
    void test02InitialBalanceIsNotNegative() {
        GiftCard card = new GiftCard("martina", "1",1000);
        assertFalse(card.balance() < 0);
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "1",-3));
    }

    @Test
    void test03ChargesCorrectly() {
        GiftCard card = new GiftCard("martina", "1",1000);
        card.charge(500, "kiosco");
        assertEquals(500, card.balance());
    }

    @Test
    void test04CantChargeMoreThanBalance() {
        GiftCard card = new GiftCard("martina", "1",1000);
        assertThrows(IllegalArgumentException.class, () -> card.charge(2000, "kiosco"));
    }

    @Test
    void test05CanNotChargeANegativeBalance() {
        GiftCard card = new GiftCard("martina", "1",1000);
        assertThrows(IllegalArgumentException.class, () -> card.charge(-500,  "kiosco"));
    }

    @Test
    void test06CanNotAddANegativeBalance() {
        GiftCard card = new GiftCard("martina", "1",1000);
        assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500));
    }

    @Test
    void test07AddsBalanceCorrectly() {
        GiftCard card = new GiftCard("martina", "1",1000);
        card.addBalance(1000);
        assertEquals(2000, card.balance());
    }

    @Test
    void test08Creates2GiftCardCorrectly() {
        GiftCard card1 = new GiftCard("martina", "1",1000);
        GiftCard card2 = new GiftCard("martina", "2",100);
        assertEquals(1000, card1.balance());
        assertEquals(100, card2.balance());
    }

    @Test
    void test09RisesErrorsWhenMakingTransactionsWith0Money() {
        GiftCard card = new GiftCard("martina", "1", 1000);
        assertThrows(IllegalArgumentException.class, () -> card.addBalance(0));
        assertThrows(IllegalArgumentException.class, () -> card.charge(0, "nada"));
    }

}
