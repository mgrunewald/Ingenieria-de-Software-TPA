package org.udesa.tpa;

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
    void test01CreatesTokenAndSetsTTL() {
        Duration ttl = Duration.ofMinutes(5);
        UserSession session = UserSession.issue("martina", ttl, clock);
        assertNotNull(session.token());
        assertEquals("martina", session.username());
        assertEquals(Instant.parse("2025-09-15T20:05:00Z"), session.issuedAt().plus(ttl));
        assertTrue(session.isActive(clock));
    }

    @Test
    void test02SessionExpiresAt5Min() {
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);
        clock.plus(Duration.ofMinutes(5).plusSeconds(1));
        assertFalse(session.isActive(clock));
    }

    @Test
    void tets03RisesErrorWhenTokenIsExpired() {
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);
        clock.plus(Duration.ofMinutes(6));
        assertThrows(IllegalArgumentException.class, () -> session.ensureActive(clock));
    }

    @Test
    void test04FailsWhenValuesAreInvalid() {
        assertThrows(IllegalArgumentException.class, () -> UserSession.issue(null, Duration.ofMinutes(5), clock));
        assertThrows(IllegalArgumentException.class, () -> UserSession.issue("  ", Duration.ofMinutes(5), clock));
        assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ZERO, clock));
        assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ofMinutes(-1), clock));
        assertThrows(IllegalArgumentException.class, () -> UserSession.issue("martina", Duration.ofMinutes(5), null));
    }

}