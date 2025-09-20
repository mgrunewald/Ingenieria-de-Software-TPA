package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import static org.udesa.tpa.FacadeTest.*;
import static org.udesa.tpa.Facade.*;
import static org.udesa.tpa.UserSession.*;
import static org.junit.jupiter.api.Assertions.*;

class  UserSessionTest {
    private MyClock clock;

    @BeforeEach
    void createClock() {
        clock = new MyClock(Instant.parse("2025-09-15T20:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void test01createsTokenAndSetsTTL() {
        Duration ttl = Duration.ofMinutes(5);
        UserSession session = UserSession.issue(USER_1, ttl, clock);
        assertNotNull(session.token());
        assertEquals(USER_1, session.username());
        assertEquals(clock.instant().plus(ttl), session.issuedAt().plus(ttl));
        assertTrue(session.isActive(clock));
    }

    @Test
    void test02sessionIsActiveAtFiveMinutesAndExpiresAfter() {
        UserSession session = UserSession.issue(USER_1, Duration.ofMinutes(5), clock);
        clock.plus(Duration.ofMinutes(4).plusSeconds(59));
        assertTrue(session.isActive(clock));
        clock.plus(Duration.ofSeconds(1));
        assertTrue(session.isActive(clock));
        clock.plus(Duration.ofSeconds(1));
        assertFalse(session.isActive(clock));
    }


    @Test
    void test03raisesErrorWhenTokenIsExpired() {
        UserSession session = UserSession.issue(USER_1, Duration.ofMinutes(5), clock);
        clock.plus(Duration.ofMinutes(6));
        assertThrowsLike(() -> session.ensureActive(clock), EXPIRED_TOKEN);
    }

    @Test
    void test04failsWhenValuesAreInvalid() {
        assertThrowsLike(() -> UserSession.issue(null, Duration.ofMinutes(5), clock), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> UserSession.issue("  ", Duration.ofMinutes(5), clock), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> UserSession.issue(USER_1, Duration.ZERO, clock), INVALID_TTL);
        assertThrowsLike(() -> UserSession.issue(USER_1, Duration.ofMinutes(-1), clock), INVALID_TTL);
        assertThrowsLike(() -> UserSession.issue(USER_1, Duration.ofMinutes(5), null), NULL_OBJECT);
    }
}
