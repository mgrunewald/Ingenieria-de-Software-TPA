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
    void tes07createsTwoGiftCardsCorrectly() {
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

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void test09failsToCreateGiftCardWithInvalidOwner(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(invalid, "1", 100));
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void test10failsToCreateGiftCardWithInvalidCardNumber(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("martina", invalid, 100));
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

