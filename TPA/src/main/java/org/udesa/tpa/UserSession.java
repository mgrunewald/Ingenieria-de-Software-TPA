package org.udesa.tpa;

import java.time.*;
import java.util.UUID;
import static org.springframework.util.Assert.*;

public final class UserSession {
    private final String token;
    private final String username;
    private final Instant issuedAt;
    private final Instant expiresAt;

    private UserSession(String token, String username, Instant issuedAt, Instant expiresAt) {
        this.token = token;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        notNull(token, "Token nulo");
        notNull(username, "Username nulo");
        notNull(issuedAt, "issuedAt nulo");
        notNull(expiresAt, "expiresAt nulo");
    }

    public static UserSession issue(String username, Duration ttl, Clock clock) {
        notNull(ttl, "TTL nulo");
        notNull(clock, "Clock nulo");
        isTrue(!username.isBlank(), "Username vacío");
        isTrue(!ttl.isNegative() && !ttl.isZero(), "TTL inválido");

        Instant now = Instant.now(clock);
        String token = UUID.randomUUID().toString();
        return new UserSession(token, username, now, now.plus(ttl));
    }

    public boolean isActive(Clock clock) {
        return !Instant.now(clock).isAfter(expiresAt);
    }

    public void ensureActive(Clock clock) {
        isTrue(isActive(clock), "Sesión expirada o inválida");
    }

    public String token()     { return token; }
    public String username()  { return username; }
    public Instant issuedAt() { return issuedAt; }
    public Instant expiresAt(){ return expiresAt; }
}
