package org.udesa.tpa;

import java.time.*;
import java.util.UUID;

import static org.springframework.util.Assert.*;
import static org.udesa.tpa.Utils.*;
import static org.udesa.tpa.Facade.*;

public final class UserSession {
    private final String token;
    private final String username;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final Duration ttl;

    public static String INVALID_TTL = "TTL can not be negative nor zero";
    public static String EXPIRED_TOKEN = "Token has expired";

    private UserSession(String token, String username, Instant issuedAt, Instant expiresAt) {
        notNull(issuedAt, NULL_OBJECT);
        notNull(expiresAt, NULL_OBJECT);
        this.token = nonBlank(token, NULL_OR_EMPTY_VALUE);
        this.username = nonBlank(username, NULL_OR_EMPTY_VALUE);
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ttl = Duration.between(issuedAt, expiresAt);
    }

    public static UserSession issue(String username, Duration ttl, Clock clock) {
        nonBlank(username, NULL_OR_EMPTY_VALUE);
        notNull(ttl, NULL_OBJECT);
        notNull(clock, NULL_OBJECT);
        ensure(!ttl.isNegative() && !ttl.isZero(), INVALID_TTL);

        Instant now = Instant.now(clock);
        String token = UUID.randomUUID().toString();
        return new UserSession(token, username, now, now.plus(ttl));
    }

    public boolean isActive(Clock clock) {
        notNull(clock, NULL_OBJECT);
        return !Instant.now(clock).isAfter(expiresAt);
    }

    public void ensureActive(Clock clock) {
        notNull(clock, NULL_OBJECT);
        ensure(!Instant.now(clock).isAfter(expiresAt), EXPIRED_TOKEN);
    }

    public String token()     { return token; }
    public String username()  { return username; }
    public Instant issuedAt() { return issuedAt; }
}
