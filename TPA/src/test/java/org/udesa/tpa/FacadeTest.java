package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    private Facade facade;

    @BeforeEach
    public void createFacade() {
        facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
    }

    @Test
    void test01UnregisteredUsernameIsMissing() {
        assertFalse(facade.exists("martina"));
    }

    @Test
    void test02RegistersUsernameCorrectly() {
        facade.register("martina", "12345678");
        assertTrue(facade.exists("martina"));
    }

    @Test
    void test03StartsSessionCorrectly() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        assertTrue(facade.isSessionActive(token));
    }

    @Test
    void test04FailsToLoginWithIncorrectPassword() {
        facade.register("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.login("martina", "otra"));
    }

    @Test
    void test05UnregisteredUserFailsToLogin() {
        assertThrows(IllegalArgumentException.class, () -> facade.login("maximo", "password123"));
    }

    @Test
    void test06TokenIsValid() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        assertFalse(isNull(token));
    }

    @Test
    void test07FailsToRegisterUsersWithTheSameUsername() {
        facade.register("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.register("martina", "abcdefgh"));
    }

    @Test
    void test08ShowsOnlyTheGiftCardsOfTheUser() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("martina", "2", 500));
        facade.preloadGiftCard(new GiftCard("maxi",    "3", 200));
        String token = facade.login("martina", "12345678");
        facade.claim(token, "1");
        facade.claim(token, "2");
        List<String> mine = facade.myCards(token);
        assertEquals(2, mine.size());
        assertTrue(mine.contains("1"));
        assertTrue(mine.contains("2"));
        assertFalse(mine.contains("3"));
        assertEquals(1500, totalBalance(facade, token));
    }

    @Test
    void test09DoesNotOperateWithExpiredToken() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register("martina", "x");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        String token = facade.login("martina", "x");
        facade.claim(token, "1");
        clock.plus(Duration.ofMinutes(6));
        assertFalse(facade.isSessionActive(token));
        assertThrows(IllegalArgumentException.class, () -> facade.myCards(token));
        assertThrows(IllegalArgumentException.class, () -> facade.balance(token, "1"));
        assertThrows(IllegalArgumentException.class, () -> facade.statement(token, "1"));
    }

    @Test
    void test10ShowsDataOnlyIfTheUserIsTheOwner() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("maxi",    "2",  500));
        String tMartina = facade.login("martina", "12345678");
        facade.claim(tMartina, "1");
        assertEquals(1000, facade.balance(tMartina, "1"));
        assertEquals(0, facade.statement(tMartina, "1").size());
        assertThrows(IllegalArgumentException.class, () -> facade.balance(tMartina, "2"));
        assertThrows(IllegalArgumentException.class, () -> facade.statement(tMartina, "2"));
    }

    @Test
    void test11DoesNotAllowTwoGiftCardsWithTheSameNumberWithDifferentOwners() {
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");
        String token1 = facade.login("martina", "12345678");
        String token2 = facade.login("maxi", "abcdefgh");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertThrows(IllegalArgumentException.class, () -> facade.preloadGiftCard(new GiftCard("maxi", "1", 500)));
    }

    @Test
    void test12DoesNotAllowTwoGiftCardsWithTheSameNumberWithTheSameOwner() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertThrows(IllegalArgumentException.class, () -> facade.preloadGiftCard(new GiftCard("martina", "1", 500)));
    }

    @Test
    void test13DoesNotAllowTwoMerchantsWithTheSameId () {
        facade.registerMerchant("mercado-pago", "galperin123");
        assertThrows(IllegalArgumentException.class, () -> facade.registerMerchant("mercado-pago", "marquitosgalpe"));
    }

    @Test
    void test14CanNotClaimACardMoreThanOnceOnTheSameSession() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.claim(token, "1");
        assertThrows(IllegalArgumentException.class, () -> facade.claim(token, "1"));
    }

    @Test
    void test15FailsToReadBalanceOfAnUnclaimedCard() {
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        assertThrows(IllegalArgumentException.class, () -> facade.balance(token, "1"));
        assertThrows(IllegalArgumentException.class, () -> facade.statement(token, "1"));
    }

    // helper privado para test07
    private static int totalBalance(Facade facade, String token) {
        return facade.myCards(token).stream().mapToInt(cardNumber -> facade.balance(token, cardNumber)).sum();
    }
}

