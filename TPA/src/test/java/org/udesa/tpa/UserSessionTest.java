package org.udesa.tpa;

import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void test01createsTokenAndSetsTTL() {
        Duration ttl = Duration.ofMinutes(5);
        UserSession session = UserSession.issue("martina", ttl, clock);
        assertAll(
                () -> assertNotNull(session.token()),
                () -> assertEquals("martina", session.username()),
                () -> assertEquals(clock.instant().plus(ttl), session.issuedAt().plus(ttl)),
                () -> assertTrue(session.isActive(clock))
        );
    }

    @Test
    void test02sessionIsActiveAtFiveMinutesAndExpiresAfter() {
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);

        clock.plus(Duration.ofMinutes(4).plusSeconds(59));
        assertTrue(session.isActive(clock));

        clock.plus(Duration.ofSeconds(1));
        assertTrue(session.isActive(clock));

        clock.plus(Duration.ofSeconds(1));
        assertFalse(session.isActive(clock));
    }


    @Test
    void test03raisesErrorWhenTokenIsExpired() {
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);
        clock.plus(Duration.ofMinutes(6));
        assertThrows(IllegalArgumentException.class, () -> session.ensureActive(clock));
    }

    @Test
    void test04failsWhenValuesAreInvalid() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> UserSession.issue(null, Duration.ofMinutes(5), clock)),
                () -> assertThrows(IllegalArgumentException.class, () -> UserSession.issue("  ", Duration.ofMinutes(5), clock)),
                () -> assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ZERO, clock)),
                () -> assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ofMinutes(-1), clock)),
                () -> assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ofMinutes(5), null))
        );
    }
}
