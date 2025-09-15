package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;
import java.time.*;
import java.util.*;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


public class Facade {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, UserSession> sessions = new HashMap<>();
    private final Map<String, List<Charge>> charges = new HashMap<>(); // cardNumber -> cargos
    private final Clock clock;
    private final Duration ttl;

    public Facade() { this(Clock.systemUTC(), Duration.ofMinutes(5)); }
    public Facade(Clock clock, Duration ttl) {
        this.clock = Objects.requireNonNull(clock);
        this.ttl = Objects.requireNonNull(ttl);
    }
    private final Map<String, GiftCard> GiftCardsByCardNumber = new HashMap<>();

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

    private String requireActiveUsername(String sessionToken) {
        isTrue(isSessionActive(sessionToken), "Sesión expirada o inválida");
        return sessions.get(sessionToken).username();
    }

    public void preloadGiftCard(GiftCard card) {
        notNull(card, "GiftCard nula");
        isTrue(!GiftCardsByCardNumber.containsKey(card.cardNumber()), "Card number duplicado");
        GiftCardsByCardNumber.put(card.cardNumber(), card);
    }

    public List<GiftCard> listGiftCards(String sessionToken) {
        String username = requireActiveUsername(sessionToken);
        List<GiftCard> cards = new ArrayList<>();
        for (GiftCard card : GiftCardsByCardNumber.values()) {
            if (card.owner().equals(username)) cards.add(card);
        }
        return List.copyOf(cards);
    }

    public int totalBalance(String sessionToken) {
        return listGiftCards(sessionToken).stream().mapToInt(GiftCard::balance).sum();
    }

    public record Charge(String merchantKey, String cardNumber, int amount, String description, Instant at) {}

    public int balance(String sessionToken, String cardNumber) {
        String user = requireActiveUsername(sessionToken);
        GiftCard card = GiftCardsByCardNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        isTrue(card.owner().equals(user), "Gift card no pertenece al usuario");
        return card.balance();
    }

    public List<Charge> statement(String sessionToken, String cardNumber) {
        String user = requireActiveUsername(sessionToken);
        GiftCard card = GiftCardsByCardNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        isTrue(card.owner().equals(user), "Gift card no pertenece al usuario");
        return List.copyOf(charges.getOrDefault(cardNumber, List.of()));
    }

    public Optional<String> usernameOf(String sessionToken) {
        UserSession s = sessions.get(sessionToken);
        if (s == null) return Optional.empty();
        if (!s.isActive(clock)) { sessions.remove(sessionToken); return Optional.empty(); }
        return Optional.of(s.username());
    }

}