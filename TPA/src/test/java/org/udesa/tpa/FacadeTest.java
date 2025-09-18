package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    private Facade facade;

    @BeforeEach
    void createFacade() {
        facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
    }

    @Test
    void test01unregisteredUsernameIsMissing() {
        assertFalse(facade.exists("martina"));
    }

    @Test
    void test02registersUsernameCorrectly() {
        facade.register("martina", "12345678");
        assertTrue(facade.exists("martina"));
    }

    @Test
    void test03startsSessionCorrectly() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        assertAll(
                () -> assertTrue(facade.isSessionActive(token)),
                () -> assertNotNull(token)
        );
    }

    @Test
    void test04failsToLoginWithIncorrectPassword() {
        facade.register("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.login("martina", "otra"));
    }

    @Test
    void test05unregisteredUserFailsToLogin() {
        assertThrows(IllegalArgumentException.class, () -> facade.login("maximo", "password123"));
    }

    @Test
    void test06failsToRegisterUsersWithTheSameUsername() {
        facade.register("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.register("martina", "abcdefgh"));
    }

    @Test
    void test07showsOnlyTheGiftCardsOfTheUser() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("martina", "2",  500));
        facade.preloadGiftCard(new GiftCard("maxi",    "3",  200));
        String token = facade.login("martina", "12345678");
        facade.claim(token, "1");
        facade.claim(token, "2");
        List<String> mine = facade.myCards(token);
        assertAll(
                () -> assertEquals(2, mine.size()),
                () -> assertTrue(mine.contains("1")),
                () -> assertTrue(mine.contains("2")),
                () -> assertFalse(mine.contains("3")),
                () -> assertEquals(1500, totalBalance(facade, token))
        );
    }

    @Test
    void test08doesNotOperateWithExpiredToken() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register("martina", "x");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        String token = facade.login("martina", "x");
        facade.claim(token, "1");

        clock.plus(Duration.ofMinutes(6));
        assertAll(
                () -> assertFalse(facade.isSessionActive(token)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.myCards(token)),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(token, "1")),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(token, "1"))
        );
    }

    @Test
    void test09showsDataOnlyIfTheUserIsTheOwner() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("maxi",    "2",  500));
        String tMartina = facade.login("martina", "12345678");
        facade.claim(tMartina, "1");
        assertAll(
                () -> assertEquals(1000, facade.balance(tMartina, "1")),
                () -> assertEquals(0,    facade.statement(tMartina, "1").size()),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(tMartina, "2")),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(tMartina, "2"))
        );
    }

    @Test
    void test10doesNotAllowTwoGiftCardsWithTheSameNumberWithDifferentOwners() {
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        facade.login("martina", "12345678");
        facade.login("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertThrows(IllegalArgumentException.class,
                () -> facade.preloadGiftCard(new GiftCard("maxi", "1", 500)));
    }

    @Test
    void test11doesNotAllowTwoGiftCardsWithTheSameNumberWithTheSameOwner() {
        facade.register("martina", "12345678");
        facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertThrows(IllegalArgumentException.class,
                () -> facade.preloadGiftCard(new GiftCard("martina", "1", 500)));
    }

    @Test
    void test12doesNotAllowTwoMerchantsWithTheSameId () {
        facade.registerMerchant("mercado-pago", "galperin123");
        assertThrows(IllegalArgumentException.class,
                () -> facade.registerMerchant("mercado-pago", "marquitosgalpe"));
    }

    @Test
    void test13canNotClaimACardMoreThanOnceOnTheSameSession() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.claim(token, "1");
        assertThrows(IllegalArgumentException.class, () -> facade.claim(token, "1"));
    }

    @Test
    void test14failsToReadBalanceOfAnUnclaimedCard() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> facade.balance(token, "1")),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.statement(token, "1"))
        );
    }



    @Test
    void test15merchantWithValidKeyCanChargeClaimedCardAndUpdatesBalanceAndStatement_noToken() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-18T12:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));

        facade.register("martina", "x");
        facade.registerMerchant("mp", "cred");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));

        String token = facade.login("martina", "x");
        facade.claim(token, "1");

        facade.charge("mp", "cred", "1", 300, "cafe");

        assertAll(
                () -> assertEquals(700, facade.balance(token, "1")),
                () -> assertEquals(1, facade.statement(token, "1").size()),
                () -> assertEquals(300, facade.statement(token, "1").get(0).amount()),
                () -> assertEquals("cafe", facade.statement(token, "1").get(0).description()),

                () -> assertEquals(Instant.parse("2025-09-18T12:00:00Z"),
                        facade.statement(token, "1").get(0).timestamp())
        );
    }

    @Test
    void test16rejectChargeOnUnclaimedCard_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));

        facade.register("martina", "x");
        facade.registerMerchant("mp", "cred");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));


        assertThrows(IllegalArgumentException.class, () -> facade.charge("mp", "cred", "1", 100, "x"));
    }

    @Test
    void test17rejectChargeFromUnknownMerchantKey_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));

        facade.register("martina", "x");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        String token = facade.login("martina", "x");
        facade.claim(token, "1");

        assertThrows(IllegalArgumentException.class, () -> facade.charge("desconocido", "cred", "1", 100, "x"));
    }

    @Test
    void test18rejectNonPositiveChargeAmount_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "x");
        facade.registerMerchant("mp", "cred");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        String token = facade.login("martina", "x");
        facade.claim(token, "1");
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> facade.charge("mp", "cred", "1", 0,  "x")),
                () -> assertThrows(IllegalArgumentException.class, () -> facade.charge("mp", "cred", "1", -1, "x"))
        );
    }

    @Test
    void test19rejectChargeWithWrongMerchantCredential_noToken() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.registerMerchant("mp", "cred-buena");
        facade.register("martina", "x");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        String token = facade.login("martina", "x");
        facade.claim(token, "1");

        assertThrows(IllegalArgumentException.class, () -> facade.charge("mp", "cred-mala", "1", 100, "x"));
    }

    @Test
    void test20merchantChargeWithTokenVariantAlsoWorks() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-18T12:34:56Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));

        facade.register("martina", "x");
        facade.registerMerchant("mp", "cred");
        facade.preloadGiftCard(new GiftCard("martina", "1", 500));

        String token = facade.login("martina", "x");
        facade.claim(token, "1");

        facade.charge(token, "mp", "cred", "1", 200, "compra");

        assertAll(
                () -> assertEquals(300, facade.balance(token, "1")),
                () -> assertEquals(1, facade.statement(token, "1").size()),
                () -> assertEquals(200, facade.statement(token, "1").get(0).amount()),
                () -> assertEquals("compra", facade.statement(token, "1").get(0).description()),
                () -> assertEquals(Instant.parse("2025-09-18T12:34:56Z"),
                        facade.statement(token, "1").get(0).timestamp())
        );
    }

    // helper privado para balance total
    private static int totalBalance(Facade facade, String token) {
        return facade.myCards(token).stream()
                .mapToInt(cardNumber -> facade.balance(token, cardNumber))
                .sum();
    }
}

