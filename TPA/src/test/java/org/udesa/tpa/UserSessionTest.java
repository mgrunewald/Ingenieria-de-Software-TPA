// UserSessionTest.java
package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    @Test
    void test01CreatesTokenAndSetsTTL() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        Duration ttl = Duration.ofMinutes(5);

        UserSession session = UserSession.issue("martina", ttl, clock);

        assertNotNull(session.token());
        assertEquals("martina", session.username());
        assertEquals(Instant.parse("2025-09-15T20:05:00Z"), session.expiresAt());
        assertTrue(session.isActive(clock));
    }

    @Test
    void test02SessionExpiresAt5Min() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);

        clock.plus(Duration.ofMinutes(5).plusSeconds(1));
        assertFalse(session.isActive(clock));
    }

    @Test
    void tets03RisesErrorWhenTokenIsExpired() {
        MutableClock clock = new MutableClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
        UserSession session = UserSession.issue("martina", Duration.ofMinutes(5), clock);

        clock.plus(Duration.ofMinutes(6));
        assertThrows(IllegalArgumentException.class, () -> session.ensureActive(clock));
    }
}
