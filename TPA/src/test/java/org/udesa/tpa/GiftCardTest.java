package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.udesa.tpa.FacadeTest.*;
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
        assertAll(
                () -> assertEquals(USER_1, card.owner()),
                () -> assertEquals(1000, card.balance())
        );
    }

    @Test
    void test02initialBalanceIsNotNegative() {
        assertAll(
                () -> assertEquals(1000, card.balance()),
                () -> assertThrows(IllegalArgumentException.class, () -> new GiftCard(USER_1, CARD_NUMBER_1, -3))
        );
    }

    @Test
    void test03chargesCorrectly() {
        card.charge(500, CHARGE_DESCRIPTION);
        assertEquals(500, card.balance());
    }

    @Test
    void test04cantChargeMoreThanBalance() {
        assertThrows(IllegalArgumentException.class, () -> card.charge(2000, CHARGE_DESCRIPTION));
    }

    @Test
    void test05cannotChargeOrAddZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.addBalance(-500)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(0, CHARGE_DESCRIPTION)),
                () -> assertThrows(IllegalArgumentException.class, () -> card.charge(-500, CHARGE_DESCRIPTION))
        );
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
        assertThrows(IllegalArgumentException.class, () -> new GiftCard("", CARD_NUMBER_1, 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(" ", CARD_NUMBER_1, 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(null, CARD_NUMBER_1, 100));

    }

    @Test
    void test10failsToCreateGiftCardWithInvalidCardNumber(){
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(USER_1, "", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(USER_1, " ", 100));
        assertThrows(IllegalArgumentException.class, () -> new GiftCard(USER_1, null, 100));
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
}

// mismo usuareio hace charges de 2 giftcards y 2 usuariosb charge sis gc a l mismo tiempo
// fallar al hacer el charge de un usuario que no es el sduyo
