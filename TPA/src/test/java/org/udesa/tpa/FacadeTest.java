package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

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
        facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
    }

    @Test
    void test01unregisteredUsernameIsMissing() {
        assertFalse(facade.exists(USER_1));
    }

    @Test
    void test02registersUsernameCorrectly() {
        facade.register(USER_1, PASSWORD_1);
        assertTrue(facade.exists(USER_1));
    }

    @Test
    void test03startsSessionCorrectly() {
        String token = registerAndLoginUser(USER_1, PASSWORD_1);
        assertAll(
                () -> assertTrue(facade.isSessionActive(token)),
                () -> assertNotNull(token)
        );
    }

    @Test
    void test04failsToLoginWithIncorrectPassword() {
        facade.register(USER_1, PASSWORD_1);
        assertThrows(IllegalArgumentException.class, () -> facade.login(USER_1, "otra"));
    }

    @Test
    void test05unregisteredUserFailsToLogin() {
        assertThrows(IllegalArgumentException.class, () -> facade.login(USER_2, PASSWORD_2));
    }

    @Test
    void test06failsToRegisterUsersWithTheSameUsername() {
        facade.register(USER_1, PASSWORD_1);
        assertThrows(IllegalArgumentException.class, () -> facade.register(USER_1, PASSWORD_2));
    }

    @Test
    void test07showsOnlyTheGiftCardsOfTheUser() {
        registerAndPreloadGiftCard(USER_1, PASSWORD_1, CARD_NUMBER_1, 1000);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_2,  500));
        registerAndPreloadGiftCard(USER_2, PASSWORD_2, CARD_NUMBER_3, 200);
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        facade.claim(token, CARD_NUMBER_2);
        List<String> mine = facade.myCards(token);
        assertAll(
                () -> assertEquals(2, mine.size()),
                () -> assertTrue(mine.contains(CARD_NUMBER_1)),
                () -> assertTrue(mine.contains(CARD_NUMBER_2)),
                () -> assertFalse(mine.contains(CARD_NUMBER_3)),
                () -> assertEquals(1500, totalBalance(facade, token))
        );
    }

    @Test
    void test08doesNotOperateWithExpiredToken() {
        MyClock clock = new MyClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);

        clock.plus(Duration.ofMinutes(6));
        assertAll(
                () -> assertFalse(facade.isSessionActive(token)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.myCards(token)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(token, CARD_NUMBER_1)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(token, CARD_NUMBER_1))
        );
    }

    @Test
    void test09showsDataOnlyIfTheUserIsTheOwner() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.register(USER_2, PASSWORD_2);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        facade.preloadGiftCard(new GiftCard(USER_2, CARD_NUMBER_2,  500));
        String tMartina = facade.login(USER_1, PASSWORD_1);
        facade.claim(tMartina, CARD_NUMBER_1);
        assertAll(
                () -> assertEquals(1000, facade.balance(tMartina, CARD_NUMBER_1)),
                () -> assertEquals(0,    facade.statement(tMartina, CARD_NUMBER_1).size()),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(tMartina, CARD_NUMBER_2)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(tMartina, CARD_NUMBER_2))
        );
    }

    @Test
    void test10doesNotAllowTwoGiftCardsWithTheSameNumberWithDifferentOwners() {
        registerPreloadGiftCardAndLoginUser(USER_1, PASSWORD_1, CARD_NUMBER_1, 1000);
        registerAndLoginUser(USER_2, PASSWORD_2);
        assertThrows(IllegalArgumentException.class,
                () -> facade.preloadGiftCard(new GiftCard(USER_2, CARD_NUMBER_1, 500)));
    }

    @Test
    void test11doesNotAllowTwoGiftCardsWithTheSameNumberWithTheSameOwner() {
        registerPreloadGiftCardAndLoginUser(USER_1, PASSWORD_1, CARD_NUMBER_1, 1000);
        assertThrows(IllegalArgumentException.class,
                () -> facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 500)));
    }

    @Test
    void test12doesNotAllowTwoMerchantsWithTheSameId () {
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        assertThrows(IllegalArgumentException.class,
                () -> facade.registerMerchant(MERCHANT_ID_1, "marquitosgalpe"));
    }

    @Test
    void test13canNotClaimACardMoreThanOnceOnTheSameSession() {
        String token = registerPreloadGiftCardAndLoginUser(USER_1, PASSWORD_1, CARD_NUMBER_1, 1000);
        facade.claim(token, CARD_NUMBER_1);
        assertThrows(IllegalArgumentException.class, () -> facade.claim(token, CARD_NUMBER_1));
    }

    @Test
    void test14failsToReadBalanceOfAnUnclaimedCard() {
        String token = registerPreloadGiftCardAndLoginUser(USER_1, PASSWORD_1, CARD_NUMBER_1, 1000);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(token, CARD_NUMBER_1)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(token, CARD_NUMBER_1))
        );
    }

    @Test
    void test15merchantWithValidKeyCanChargeClaimedCardAndUpdatesBalanceAndStatement_noToken() {
        MyClock clock = new MyClock(Instant.parse("2025-09-18T12:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
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
    void test16rejectChargeOnUnclaimedCard_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        assertThrows(IllegalArgumentException.class, () -> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 100, PASSWORD_1));
    }

    @Test
    void test17rejectChargeFromUnknownMerchantKey_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrows(IllegalArgumentException.class, () -> facade.charge("unknown", MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 100, PASSWORD_1));
    }

    @Test
    void test18rejectNonPositiveChargeAmount_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 0, PASSWORD_1)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.charge(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, -1, PASSWORD_1))
        );
    }

    @Test
    void test19rejectChargeWithWrongMerchantCredential_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        facade.register(USER_1, PASSWORD_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 1000));
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        assertThrows(IllegalArgumentException.class, () -> facade.charge(MERCHANT_ID_1, "otra", CARD_NUMBER_1, 100, PASSWORD_1));
    }

    @Test
    void test20merchantChargeWithTokenVariantAlsoWorks() {
        MyClock clock = new MyClock(Instant.parse("2025-09-18T12:34:56Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register(USER_1, PASSWORD_1);
        facade.registerMerchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        facade.preloadGiftCard(new GiftCard(USER_1, CARD_NUMBER_1, 500));
        String token = facade.login(USER_1, PASSWORD_1);
        facade.claim(token, CARD_NUMBER_1);
        facade.charge(token, MERCHANT_ID_1, MERCHANT_CREDENTIAL_1, CARD_NUMBER_1, 200, CHARGE_DESCRIPTION);
        assertAll(
                () -> assertEquals(300, facade.balance(token, CARD_NUMBER_1)),
                () -> assertEquals(1, facade.statement(token, CARD_NUMBER_1).size()),
                () -> assertEquals(200, facade.statement(token, CARD_NUMBER_1).get(0).amount()),
                () -> assertEquals(CHARGE_DESCRIPTION, facade.statement(token, CARD_NUMBER_1).get(0).description()),
                () -> assertEquals(Instant.parse("2025-09-18T12:34:56Z"),
                        facade.statement(token, CARD_NUMBER_1).get(0).timestamp())
        );
    }

    private String registerAndLoginUser(String username, String password) {
        facade.register(username, password);
        return facade.login(username, password);
    }

    private void registerAndPreloadGiftCard(String username, String password, String cardNumber, int initialBalance) {
        facade.register(username, password);
        facade.preloadGiftCard(new GiftCard(username, cardNumber, initialBalance));
    }

    private String registerPreloadGiftCardAndLoginUser(String username, String password, String cardNumber, int initialBalance) {
        registerAndPreloadGiftCard(username, password, cardNumber, initialBalance);
        return facade.login(username, password);
    }

    // helper privado para balance total
    private static int totalBalance(Facade facade, String token) {
        return facade.myCards(token).stream()
                .mapToInt(cardNumber -> facade.balance(token, cardNumber))
                .sum();
    }
}

