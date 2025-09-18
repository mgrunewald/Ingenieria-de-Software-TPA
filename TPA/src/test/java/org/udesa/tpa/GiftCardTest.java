package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.TestFixtures.*;

public class GiftCardTest {
    private GiftCard card;

    @BeforeEach
    void createCard() {
        card = gcMartina1000();
    }

    @Test
    void createsGiftCardCorrectlyWithOwnerAndInitialBalance() {
        assertAll(
                () -> assertEquals("martina", card.owner()),
                () -> assertEquals(1_000, card.balance())
        );
    }

    @Test
    void initialBalanceIsNotNegative() {
        assertAll(
                () -> assertEquals(1_000, card.balance()),
                () -> assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", "1", -3))
        );
    }

    @Test
    void chargesCorrectly() {
        card.charge(500, "kiosco");
        assertEquals(500, card.balance());
    }

    @Test
    void cantChargeMoreThanBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(2_000, "kiosco"));
    }

    @Test
    void cannotChargeOrAddZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(0, "nada")),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(-500, "nada"))
        );
    }

    @Test
    void addsBalanceCorrectly() {
        card.addBalance(1_000);
        assertEquals(2_000, card.balance());
    }

    @Test
    void createsTwoGiftCardsCorrectly() {
        var card1 = gcMartina1000();
        var card2 = gcMartina100();
        assertAll(
                () -> assertEquals(1_000, card1.balance()),
                () -> assertEquals(100,   card2.balance())
        );
    }

    @Test
    void failsToChargeWithInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(100, ""));
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void failsToCreateGiftCardWithInvalidOwner(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(invalid, "1", 100));
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void failsToCreateGiftCardWithInvalidCardNumber(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", invalid, 100));
    }

    @Test
    void chargeEqualToBalanceSetsBalanceToZero() {
        card.charge(1_000, "todo");
        assertEquals(0, card.balance());
    }

    @Test
    void addThenChargeReturnsToOriginalBalance() {
        card.addBalance(500);    // 1500
        card.charge(500, "x");   // 1000
        assertEquals(1_000, card.balance());
    }
}

