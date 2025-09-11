package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GiftCardTest {

    @Test
    void createsGiftCardWithValidToken() {
        Token token = new Token("ABCDEFGHIJ"); // 10 chars
        GiftCard card = new GiftCard(token);

        assertSame(token, card.getToken());
        assertSame(token, card.token);
    }

    @Test
    void nullTokenRisesError() {
        assertThrows(NullPointerException.class, () -> new GiftCard(null));
    }

    @Test
    void acceptsDifferentValidTokenFormats() {
        Token numeric = new Token("1234567890");
        Token mixed   = new Token("aBcD_ef-gh");

        assertDoesNotThrow(() -> new GiftCard(numeric));
        assertDoesNotThrow(() -> new GiftCard(mixed));
    }

    @Test
    void tokenValidationIsEnforcedByTokenValueObject() {
        assertThrows(IllegalArgumentException.class, () -> new Token("short")); // <10
        assertThrows(IllegalArgumentException.class, () -> new Token("loooooooooooong")); // >10
        assertThrows(IllegalArgumentException.class, () -> new Token("   "));   // blank
    }

    @Test
    void differentGiftCardsAreNotEqualByDefault() {
        GiftCard c1 = new GiftCard(new Token("AAAAAAAAAA"));
        GiftCard c2 = new GiftCard(new Token("AAAAAAAAAA"));
        assertNotEquals(c1, c2);
    }
}
