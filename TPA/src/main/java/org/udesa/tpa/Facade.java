package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.*;
import java.util.*;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


public class Facade {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, UserSession> sessions = new HashMap<>();
    private final Clock clock;
    private final Duration ttl;

    public Facade() { this(Clock.systemUTC(), Duration.ofMinutes(5)); }
    public Facade(Clock clock, Duration ttl) {
        this.clock = Objects.requireNonNull(clock);
        this.ttl = Objects.requireNonNull(ttl);
    }

    public void register(String username, String password) {
        users.put(username, password); //creo la entrada en la la lista de (usuarios, contraseña)
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public String login(String username, String password) {
        String storedPassword = users.get(username);
        notNull(storedPassword, "Usuario no existe");
        isTrue(storedPassword.equals(password), "Constraseña incorrecta");

        UserSession session = UserSession.issue(username, ttl, clock);
        sessions.put(session.token(), session);
        return session.token();
    }

    public boolean isSessionActive(String token) {
        UserSession session = sessions.get(token);
        if (session == null) return false;
        boolean active = session.isActive(clock);
        if (!active) sessions.remove(token); // limpia si venció
        return active;
    }

}