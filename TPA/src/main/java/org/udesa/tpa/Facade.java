package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;
import java.time.*;
import java.util.*;

import static org.springframework.util.Assert.*;


public class Facade {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, UserSession> sessions = new HashMap<>();
    private final Map<String, List<Charge>> charges = new HashMap<>(); // cardNumber -> cargos
    private final Map<String, GiftCard> giftCardsByCardNumber = new HashMap<>();
    private final Map<String, List<Charge>> ledger = new HashMap<>();
    private final Map<String, Merchant> merchantsByPrivateCredential = new HashMap<>();
    private final Set<String> claimed = new HashSet<>();


    private final Clock clock;
    private final Duration ttl;

    public Facade() {
        this(Clock.systemUTC(), Duration.ofMinutes(5));
    }

    public Facade(Clock clock, Duration ttl) {
        this.clock = Objects.requireNonNull(clock);
        this.ttl = Objects.requireNonNull(ttl);
    }

    public void preloadUser(String username, String password) {
        hasText(username, "username inválido");
        hasText(password, "password inválido");
        users.put(username, password);
    }

    public void preloadGiftCard(GiftCard card) {
        Objects.requireNonNull(card, "card nula");
        GiftCard prev = giftCardsByCardNumber.putIfAbsent(card.cardNumber(), card);
        isTrue(prev == null, "Gift card duplicada: " + card.cardNumber());
    }

    public void register(String username, String password) {
        users.put(username, password);
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

    private String requireActiveUsername(String token) {
        isTrue(isSessionActive(token), "Sesión expirada o inválida");
        return sessions.get(token).username();
    }

    public void claim(String token, String cardNumber) {
        String user = requireActiveUsername(token);
        GiftCard card = requireOwnedCard(user, cardNumber);
        claimed.add(card.cardNumber());
    }

    public int balance(String token, String cardNumber) {
        String user = requireActiveUsername(token);
        GiftCard card = requireOwnedCard(user, cardNumber);
        return card.balance();
    }

    public List<Charge> statement(String token, String cardNumber) {
        String user = requireActiveUsername(token);
        requireOwnedCard(user, cardNumber);
        return List.copyOf(ledger.getOrDefault(cardNumber, List.of()));
    }

    public List<String> myCards(String token) {
        String user = requireActiveUsername(token);
        List<String> result = new ArrayList<>();
        for (GiftCard c : giftCardsByCardNumber.values()) {
            if (c.owner().equals(user)) result.add(c.cardNumber());
        }
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
    }

    public List<String> myClaimedCards(String token) {
        requireActiveUsername(token);
        return claimed.stream().sorted().toList();
    }

    public void charge(String privateCredential, String cardNumber, int amount, String description) {
        Merchant m = merchantsByPrivateCredential.get(privateCredential);
        notNull(m, "Merchant inválido");

        if (!claimed.contains(cardNumber)) throw new IllegalArgumentException("Tarjeta no reclamada");

        GiftCard card = giftCardsByCardNumber.get(cardNumber);
        notNull(card, "Número de tarjeta inválido");

        card.charge(amount, description);
        ledger.computeIfAbsent(cardNumber, k -> new ArrayList<>())
                .add(new Charge(cardNumber, m.id(), amount, description, Instant.now(clock)));
    }

    private GiftCard requireOwnedCard(String username, String cardNumber) {
        GiftCard card = giftCardsByCardNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        if (!card.owner().equals(username)) throw new IllegalArgumentException("Gift card no pertenece al usuario");
        return card;
    }
}