package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.udesa.tpa.FacadeTest.*;
import static org.udesa.tpa.Facade.*;
import static org.udesa.tpa.GiftCard.*;
import static org.udesa.tpa.Charge.*;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTest {
    private GiftCard card;

    private static GiftCard gcMartina1000() { return new GiftCard(USER_1, CARD_NUMBER_1, 1000); }
    private static GiftCard gcMartina100()  { return new GiftCard(USER_1, CARD_NUMBER_2,   100); }

    @BeforeEach
    void createCard() {
        card = gcMartina1000();
    }

    @Test
    void test01createsGiftCardCorrectlyWithOwnerAndInitialBalance() {
        assertEquals(USER_1, card.owner());
        assertEquals(1000, card.balance());
    }

    @Test
    void test02initialBalanceIsNotNegative() {
        assertEquals(1000, card.balance());
        assertThrowsLike(() -> new GiftCard(USER_1, CARD_NUMBER_1, -3), NEGATIVE_INITIAL_BALANCE);
    }

    @Test
    void test03chargesCorrectly() {
        card.charge(500, CHARGE_DESCRIPTION);
        assertEquals(500, card.balance());
    }

    @Test
    void test04cantChargeMoreThanBalance() {
        assertThrowsLike(() -> card.charge(2000, CHARGE_DESCRIPTION), INSUFFICIENT_FUNDS);
    }

    @Test
    void test05cannotChargeOrAddZeroOrNegative() {
        assertThrowsLike(() -> card.addBalance(0), INVALID_AMOUNT);
        assertThrowsLike(() -> card.addBalance(-500), INVALID_AMOUNT);
        assertThrowsLike(() -> card.charge(0, CHARGE_DESCRIPTION), INVALID_AMOUNT);
        assertThrowsLike(() -> card.charge(-500, CHARGE_DESCRIPTION), INVALID_AMOUNT);
    }

    @Test
    void test06addsBalanceCorrectly() {
        card.addBalance(1000);
        assertEquals(2000, card.balance());
    }

    @Test
    void test07createsTwoGiftCardsCorrectly() {
        GiftCard card1 = gcMartina1000();
        GiftCard card2 = gcMartina100();
        assertEquals(1000, card1.balance());
        assertEquals(100,   card2.balance());
    }

    @Test
    void test08failsToChargeWithInvalidDescription() {
        assertThrowsLike(() -> card.charge(100, ""), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> card.charge(100, " "), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> card.charge(100, null), NULL_OR_EMPTY_VALUE);

    }

    @Test
    void test09failsToCreateGiftCardWithInvalidOwner(){
        assertThrowsLike(() -> new GiftCard("", CARD_NUMBER_1, 100), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> new GiftCard(" ", CARD_NUMBER_1, 100), NULL_OR_EMPTY_VALUE);
        assertThrowsLike( () -> new GiftCard(null, CARD_NUMBER_1, 100), NULL_OR_EMPTY_VALUE);
    }

    @Test
    void test10failsToCreateGiftCardWithInvalidCardNumber(){
        assertThrowsLike(() -> new GiftCard(USER_1, "", 100), NULL_OR_EMPTY_VALUE);
        assertThrowsLike( () -> new GiftCard(USER_1, " ", 100), NULL_OR_EMPTY_VALUE);
        assertThrowsLike( () -> new GiftCard(USER_1, null, 100), NULL_OR_EMPTY_VALUE);
    }

    @Test
    void test11chargeEqualToBalanceSetsBalanceToZero() {
        card.charge(1000, CHARGE_DESCRIPTION);
        assertEquals(0, card.balance());
    }

    @Test
    void test12addThenChargeReturnsToOriginalBalance() {
        card.addBalance(500);
        card.charge(500, CHARGE_DESCRIPTION);
        assertEquals(1000, card.balance());
    }

    @Test
    void test13SameUserAddsBalanceOnDifferentGiftCardsCorrectly() {
        GiftCard card1 = gcMartina1000();
        GiftCard card2 = gcMartina100();
        card1.addBalance(500);
        card2.addBalance(50);
        assertEquals(1500, card1.balance());
        assertEquals(150, card2.balance());
    }
}