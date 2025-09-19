package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    private GiftCard card;

    private static GiftCard gcMartina1000() { return new GiftCard("martina", "1", 1000); }
    private static GiftCard gcMartina100()  { return new GiftCard("martina", "2",   100); }

    @BeforeEach
    void createCard() {
        card = gcMartina1000();
    }

    @Test
    void test01createsGiftCardCorrectlyWithOwnerAndInitialBalance() {
        assertAll(
                () -> assertEquals("martina", card.owner()),
                () -> assertEquals(1000, card.balance())
        );
    }

    @Test
    void test02initialBalanceIsNotNegative() {
        assertAll(
                () -> assertEquals(1000, card.balance()),
                () -> assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "1", -3))
        );
    }

    @Test
    void test03chargesCorrectly() {
        card.charge(500, "kiosco");
        assertEquals(500, card.balance());
    }

    @Test
    void test04cantChargeMoreThanBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(2000, "kiosco"));
    }

    @Test
    void test05cannotChargeOrAddZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(0, "nada")),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(-500, "nada"))
        );
    }

    @Test
    void test06addsBalanceCorrectly() {
        card.addBalance(1000);
        assertEquals(2000, card.balance());
    }

    @Test
    void test07createsTwoGiftCardsCorrectly() {
        var card1 = gcMartina1000();
        var card2 = gcMartina100();
        assertAll(
                () -> assertEquals(1000, card1.balance()),
                () -> assertEquals(100,   card2.balance())
        );
    }

    @Test
    void test08failsToChargeWithInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(100, ""));
    }

    @Test
    void test09failsToCreateGiftCardWithInvalidOwner(){
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("", "1", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(" ", "1", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(null, "1", 100));

    }

    @Test
    void test10failsToCreateGiftCardWithInvalidCardNumber(){
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", " ", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", null, 100));
    }

    @Test
    void test11chargeEqualToBalanceSetsBalanceToZero() {
        card.charge(1000, "todo");
        assertEquals(0, card.balance());
    }

    @Test
    void test12addThenChargeReturnsToOriginalBalance() {
        card.addBalance(500);    // 1500
        card.charge(500, "x");   // 1000
        assertEquals(1000, card.balance());
    }
}

