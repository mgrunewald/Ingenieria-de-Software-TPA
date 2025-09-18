package org.udesa.tpa;

import java.time.*;
import java.util.UUID;

import static org.springframework.util.Assert.*;

public final class UserSession {
    private final String token;
    private final String username;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final Duration ttl;

    public static String NULL_INSTANT = "Instant can not be null";
    public static String NULL_DURATION = "TTL can not be null";
    public static String NULL_CLOCK = "Clock can not be null";
    public static String INVALID_TTL = "TTL can not be negative nor zero";
    public static String EXPIRED_TOKEN = "Token has expired";

    private UserSession(String token, String username, Instant issuedAt, Instant expiresAt) {
        notNull(issuedAt, NULL_INSTANT);
        notNull(expiresAt, NULL_INSTANT);
        this.token = Utils.nonBlank(token, "token");
        this.username = Utils.nonBlank(username, "username");
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ttl = Duration.between(issuedAt, expiresAt);
    }

    public static UserSession issue(String username, Duration ttl, Clock clock) {
        Utils.nonBlank(username, "username");
        notNull(ttl, NULL_DURATION);
        notNull(clock, NULL_CLOCK);
        Utils.ensure(!ttl.isNegative() && !ttl.isZero(), INVALID_TTL);

        Instant now = Instant.now(clock);
        String token = UUID.randomUUID().toString();
        return new UserSession(token, username, now, now.plus(ttl));
    }

    public boolean isActive(Clock clock) {
        notNull(clock, NULL_CLOCK);
        return !Instant.now(clock).isAfter(expiresAt);
    }

    public void ensureActive(Clock clock) {
        notNull(clock, NULL_CLOCK);
        Utils.ensure(!Instant.now(clock).isAfter(expiresAt), EXPIRED_TOKEN);
    }

    public String token()     { return token; }
    public String username()  { return username; }
    public Instant issuedAt() { return issuedAt; }
    public Instant expiresAt(){ return expiresAt; }
    public Duration ttl()     { return ttl; }
}
