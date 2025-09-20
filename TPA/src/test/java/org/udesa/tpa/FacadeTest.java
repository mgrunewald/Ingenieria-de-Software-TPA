package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import static org.udesa.tpa.Facade.*;
import static org.udesa.tpa.UserSession.*;
import static org.udesa.tpa.Charge.*;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    public static final String USER_1 = "martina";
    public static final String PASSWORD_1 = "12345678";
    public static final String USER_2 = "maximo";
    public static final String PASSWORD_2 = "abcdefgh";
    public static final String MERCHANT_ID_1 = "mercado-pago";
    public static final String MERCHANT_CREDENTIAL_1 = "galperin123";
    public static final String MERCHANT_ID_2 = "uala";
    public static final String MERCHANT_CREDENTIAL_2 = "uala123";
    public static final String CARD_NUMBER_1 = "1";
    public static final String CARD_NUMBER_2 = "2";
    public static final String CARD_NUMBER_3 = "3";
    public static final String CHARGE_DESCRIPTION = "cafe de havanna";

    private Facade facade;

    @BeforeEach
    void createFacade() {
        Map<String, String> users = Map.of(
                USER_1, PASSWORD_1,
                USER_2, PASSWORD_2
        );
        Map<String, GiftCard> cards = Map.of(
                CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000),
                CARD_NUMBER_2, new GiftCard(USER_1, CARD_NUMBER_2,  200),
                CARD_NUMBER_3, new GiftCard(USER_2, CARD_NUMBER_3,  500)
        );
        Map<String, Merchant> merchants = Map.of(
                MERCHANT_ID_1, new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1),
                MERCHANT_ID_2, new Merchant(MERCHANT_ID_2, MERCHANT_CREDENTIAL_2)
        );
        facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5), users, cards, merchants);
    }

    @Test
    void test01usersArePreloadedViaConstructorCorreclty() {
        assertTrue(facade.exists(USER_1));
        assertTrue(facade.exists(USER_2));
        assertThrowsLike(() ->  facade.exists("unregistered"), UNKNOWN_USER) ;

    }

    @Test
    void test02startsSessionCorrectly() {
        String token = facade.login(USER_1, PASSWORD_1);
        assertTrue(facade.isSessionActive(token));
        assertNotNull(token);
    }

    @Test
    void test03failsToLoginWithIncorrectPassword() {
        assertThrowsLike(() -> facade.login(USER_1, "otra"), WRONG_PASSWORD);
    }

    @Test
    void test04unregisteredUserFailsToLogin() {
        assertThrowsLike( () -> facade.login("unregistered", "password"), UNKNOWN_USER);
    }

    @Test
    void test05UnableToClaimGiftCardsThatAreNotOwnedByTheUser() {
        String token1 = facade.login(USER_1, PASSWORD_1);
        assertThrowsLike( () -> facade.claim(token1, CARD_NUMBER_3), GIFT_CARD_DOES_NOT_BELONG_TO_USER );
    }

    @Test
    void test06showsOnlyTheGiftCardsOfTheUser() {
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        facade.claim(token, CARD_NUMBER_2);
        List<String> mine = facade.myCards(token);
        assertEquals(2, mine.size());
        assertTrue(mine.contains(CARD_NUMBER_1));
        assertTrue(mine.contains(CARD_NUMBER_2));
        assertFalse(mine.contains(CARD_NUMBER_3));
        assertEquals(1200, totalBalance(facade, token));
    }

    @Test
    void test07doesNotOperateWithExpiredToken() {
        MyClock clock = new MyClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(
                clock, Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000)),
                Map.of()
        );
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);

        clock.plus(Duration.ofMinutes(6));
        assertFalse(facade.isSessionActive(token));
        assertThrowsLike(() -> facade.myCards(token), EXPIRED_TOKEN);
        assertThrowsLike(() -> facade.balance(token, CARD_NUMBER_1), EXPIRED_TOKEN);
        assertThrowsLike(() -> facade.statement(token, CARD_NUMBER_1), EXPIRED_TOKEN);
    }

    @Test
    void test08showsDataOnlyIfTheUserIsTheOwner() {
        Facade facade = new Facade(
                Clock.systemUTC(), Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1, USER_2, PASSWORD_2),
                Map.of(
                        CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000),
                        CARD_NUMBER_2, new GiftCard(USER_2, CARD_NUMBER_2,  500)
                ),
                Map.of()
        );
        String tMartina = facade.login(USER_1, PASSWORD_1);
        facade.claim(tMartina, CARD_NUMBER_1);
        assertEquals(1000, facade.balance(tMartina, CARD_NUMBER_1));
        assertEquals(0, facade.statement(tMartina, CARD_NUMBER_1).size());
        assertThrowsLike( () -> facade.balance(tMartina, CARD_NUMBER_2), UNCLAIMED_CARD);
        assertThrowsLike(() -> facade.statement(tMartina, CARD_NUMBER_2), UNCLAIMED_CARD);
    }

    @Test
    void test09canNotClaimACardMoreThanOnceOnTheSameSession() {
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrowsLike(() -> facade.claim(token, CARD_NUMBER_1), CLAIMED_CARD);
    }

    @Test
    void test10failsToReadBalanceOfAnUnclaimedCard() {
        String token = facade.login(USER_1, PASSWORD_1);
        assertThrowsLike(() -> facade.balance(token, CARD_NUMBER_1), UNCLAIMED_CARD);
        assertThrowsLike(() -> facade.statement(token, CARD_NUMBER_1), UNCLAIMED_CARD);
    }

    @Test
    void test11failsToClaimGiftCardWithInvalidNumber() {
        String token = facade.login(USER_1, PASSWORD_1);
        assertThrowsLike(() -> facade.claim(token, "otro"), UNKNOWN_CARD);

    }

    @Test
    void test12merchantWithValidKeyCanChargeClaimedCardAndUpdatesBalanceAndStatement_noToken() {
        MyClock clock = new MyClock(Instant.parse("2025-09-18T12:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(
                clock, Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000)),
                Map.of(MERCHANT_ID_1, new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1))
        );
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 300, CHARGE_DESCRIPTION);
        assertAll(
                () -> assertEquals(700, facade.balance(token, CARD_NUMBER_1)),
                () -> assertEquals(1, facade.statement(token, CARD_NUMBER_1).size()),
                () -> assertEquals(300, facade.statement(token, CARD_NUMBER_1).get(0).amount()),
                () -> assertEquals(CHARGE_DESCRIPTION, facade.statement(token, CARD_NUMBER_1).get(0).description()),
                () -> assertEquals(Instant.parse("2025-09-18T12:00:00Z"),
                        facade.statement(token, CARD_NUMBER_1).get(0).timestamp())
        );
    }

    @Test
    void test13rejectChargeOnUnclaimedCard_noToken() {
        Facade facade = new Facade(
                Clock.systemUTC(), Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000)),
                Map.of(MERCHANT_ID_1, new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1))
        );
        assertThrowsLike(()-> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 100, CHARGE_DESCRIPTION), UNCLAIMED_CARD);
    }

    @Test
    void test14rejectChargeFromUnknownMerchantKey_noToken() {
        Facade facade = new Facade(
                Clock.systemUTC(), Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000)),
                Map.of() // sin merchants
        );
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrowsLike(() -> facade.charge("unknown", MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 100, CHARGE_DESCRIPTION), UNKNOWN_MERCHANT);
    }

    @Test
    void test15rejectNonPositiveChargeAmount_noToken() {
        createFacadeForDatabaseWithOnlyOneUserCardAndMerchant(Clock.systemUTC());
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrowsLike(() -> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 0, CHARGE_DESCRIPTION), INVALID_AMOUNT);
        assertThrowsLike(() -> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, -1, CHARGE_DESCRIPTION), INVALID_AMOUNT);
    }

    @Test
    void test16rejectChargeWithWrongMerchantCredential_noToken() {
        createFacadeForDatabaseWithOnlyOneUserCardAndMerchant(Clock.systemUTC());
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrowsLike(() -> facade.charge(MERCHANT_ID_1, "otra", CARD_NUMBER_1, 100, CHARGE_DESCRIPTION), NULL_OR_EMPTY_VALUE);
    }

    @Test
    void test17merchantChargeWithTokenVariantAlsoWorks() {
        MyClock clock = new MyClock(Instant.parse("2025-09-18T12:34:56Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(
                clock, Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 500)),
                Map.of(MERCHANT_ID_1, new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1))
        );
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);

        facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 200, CHARGE_DESCRIPTION);
        assertAll(
                () -> assertEquals(300, facade.balance(token, CARD_NUMBER_1)),
                () -> assertEquals(1, facade.statement(token, CARD_NUMBER_1).size()),
                () -> assertEquals(200, facade.statement(token, CARD_NUMBER_1).get(0).amount()),
                () -> assertEquals(CHARGE_DESCRIPTION, facade.statement(token, CARD_NUMBER_1).get(0).description()),
                () -> assertEquals(Instant.parse("2025-09-18T12:34:56Z"),
                        facade.statement(token, CARD_NUMBER_1).get(0).timestamp())
        );
    }

    // helper privado para balance total
    private static int totalBalance(Facade facade, String token) {
        return facade.myCards(token).stream()
                .mapToInt(cardNumber -> facade.balance(token, cardNumber))
                .sum();
    }

    public static void assertThrowsLike(Executable executable, String message ) {
        assertEquals( message,
                assertThrows( Exception.class, executable )
                        .getMessage() );
    }

    private static void createFacadeForDatabaseWithOnlyOneUserCardAndMerchant(Clock clock){
        Facade facade = new Facade(
                clock, Duration.ofMinutes(5),
                Map.of(USER_1, PASSWORD_1),
                Map.of(CARD_NUMBER_1, new GiftCard(USER_1, CARD_NUMBER_1, 1000)),
                Map.of(MERCHANT_ID_1, new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1))
        );
    }
}