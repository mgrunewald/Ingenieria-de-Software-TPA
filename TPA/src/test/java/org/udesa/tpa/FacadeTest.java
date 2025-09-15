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
        facade = new Facade();
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
        String token = facade.login("martina", "12345678"); //arranca la sesion
        assertNotNull(token);
        assertTrue(facade.isSessionActive(token));
    }

    @Test
    void test04FailsToLoginWithIncorrectPassword(){
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
    void test07ShowsOnlyTheGiftCardsOfTheUser() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");

        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("martina", "2", 500));
        facade.preloadGiftCard(new GiftCard("maxi",    "3", 200));

        String token = facade.login("martina", "12345678");

        List<GiftCard> mine = facade.listGiftCards(token);
        assertEquals(2, mine.size());
        assertTrue(mine.stream().allMatch(gc -> gc.owner().equals("martina")));
        assertEquals(1500, facade.totalBalance(token));
    }

    @Test
    void test08DoesNotListIfTheSessionIsDue() {
        MutableClock clock = new MutableClock(
                Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Facade facade = new Facade(clock, Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));

        String token = facade.login("martina", "12345678");
        clock.plus(Duration.ofMinutes(6));

        assertFalse(facade.isSessionActive(token));
        assertThrows(IllegalArgumentException.class, () -> facade.listGiftCards(token));
    }

    @Test
    void test09ShowsDataOnlyIfTheUserIsTheOwner() {
        Facade facade = new Facade(Clock.systemUTC(), Duration.ofMinutes(5));
        facade.register("martina", "12345678");
        facade.register("maxi", "abcdefgh");

        facade.preloadGiftCard(new GiftCard("martina", "1", 1000));
        facade.preloadGiftCard(new GiftCard("maxi",    "2",  500));

        String tMartina = facade.login("martina", "12345678");
        assertEquals(1000, facade.balance(tMartina, "1"));
        assertEquals(0, facade.statement(tMartina, "1").size());

        assertThrows(IllegalArgumentException.class, () -> facade.balance(tMartina, "2"));
        assertThrows(IllegalArgumentException.class, () -> facade.statement(tMartina, "2"));
    }

}


