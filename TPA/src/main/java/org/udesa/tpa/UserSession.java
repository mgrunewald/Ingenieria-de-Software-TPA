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

    private UserSession(String token, String username, Instant issuedAt, Instant expiresAt) {
        notNull(token, "Token nulo");
        notNull(username, "Username nulo");
        notNull(issuedAt, "issuedAt nulo");
        notNull(expiresAt, "expiresAt nulo");
        this.token = token;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ttl = Duration.between(issuedAt, expiresAt);
    }

    public static UserSession issue(String username, Duration ttl, Clock clock) {
        notNull(username, "Username nulo");
        notNull(ttl, "TTL nulo");
        notNull(clock, "Clock nulo");
        isTrue(!username.isBlank(), "Username vacío");
        isTrue(!ttl.isNegative() && !ttl.isZero(), "TTL inválido");

        Instant now = Instant.now(clock);
        String token = UUID.randomUUID().toString();
        return new UserSession(token, username, now, now.plus(ttl));
    }

    public boolean isActive(Clock clock) {
        notNull(clock, "Clock nulo");
        return !Instant.now(clock).isAfter(expiresAt);
    }

    public void ensureActive(Clock clock) {
        notNull(clock, "Clock nulo");
        if (!isActive(clock)) {
            throw new IllegalArgumentException("Token expirado");
            //throw new IllegalStateException("Token expirado");
        }
    }

    public String token()     { return token; }
    public String username()  { return username; }
    public Instant issuedAt() { return issuedAt; }
    public Instant expiresAt(){ return expiresAt; }
    public Duration ttl()     { return ttl; }
}
