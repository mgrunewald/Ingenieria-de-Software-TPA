package org.udesa.tpa;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.springframework.util.Assert.*;

public final class Facade {
    private final Clock clock;
    private final Duration ttl;

    private final Map<String, String> users = new HashMap<>(); // username -> password
    private final Map<String, UserSession> sessionsByToken = new HashMap<>(); // token -> session
    private final Map<String, GiftCard> giftCardsByNumber = new HashMap<>(); // cardNumber -> GiftCard
    private final Map<String, Set<String>> claimsByToken = new HashMap<>(); // token -> set(cardNumber)
    private final Map<String, Merchant> merchantsById = new HashMap<>();      // merchantId -> Merchant
    private final Map<String, List<Charge>> chargesByCard = new HashMap<>();  // cardNumber -> charges

    public Facade(Clock clock, Duration ttl) {
        this.clock = Objects.requireNonNull(clock, "Clock no puede ser nulo");
        this.ttl = Objects.requireNonNull(ttl, "TTL no puede ser nulo");
    }

    public void register(String username, String password) {
        notNull(username, "Username no puede ser nulo");
        notNull(password, "Password no puede ser nulo");
        hasText(username, "Username no puede ser nulo");
        hasText(password, "Password no puede ser nulo");
        String prev = users.putIfAbsent(username, password);
        isTrue(prev == null, "Usuario ya registrado");
        //isTrue(!users.containsKey(username), "Usuario ya registrado");
        //users.put(username, password);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    private String requirePassword(String username) { //con esto saco los ifs en login
        var pass = users.get(username);
        notNull(pass, "Usuario inexistente");
        return pass;
    }

    public String login(String username, String password) {
        String realPass = requirePassword(username);
        isTrue(Objects.equals(realPass, password), "Password incorrecto");
        var session = UserSession.issue(username, ttl, clock);
        sessionsByToken.put(session.token(), session);
        return session.token();
    }

    public boolean isSessionActive(String token) {
        var session = sessionsByToken.get(token);
        if (session == null) return false;
        try {
            session.ensureActive(clock); // lanza IllegalArgumentException("Token expirado") si venció
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private UserSession requireActiveSession(String token) {
        var session = sessionsByToken.get(Objects.requireNonNull(token, "Token no puede ser nulo"));
        notNull(session, "Token invalido");
        session.ensureActive(clock);
        return session;
    }

    public void preloadGiftCard(GiftCard card) {
        notNull(card, "Gift card nula");
        hasText(card.cardNumber(), "Número de tarjeta vacío");
        hasText(card.owner(), "Owner vacío");

        GiftCard prev = giftCardsByNumber.putIfAbsent(card.cardNumber(), card);
        isTrue(prev == null, "Ya existe una gift card con ese número");
    }

    public void claim(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = giftCardsByNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        isTrue(card.owner().equals(session.username()), "Gift card no pertenece al usuario");
        boolean added = claimsByToken.computeIfAbsent(token, k -> new HashSet<>()).add(cardNumber);
        isTrue(added, "La tarjeta ya fue reclamada en esta sesión");
    }

    private GiftCard requireClaimed(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = giftCardsByNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        isTrue(claimsByToken.getOrDefault(token, Set.of()).contains(cardNumber), "La tarjeta no fue reclamada en esta sesión");
        isTrue(card.owner().equals(session.username()), "Gift card no pertenece al usuario");
        return card;
    }

    public List<String> myCards(String token) {
        requireActiveSession(token);
        return List.copyOf(claimsByToken.getOrDefault(token, Set.of()));
    }

    public int balanceOf(String token, String cardNumber) {
        return requireClaimed(token, cardNumber).balance();
    }

    public List<Charge> chargesOf(String token, String cardNumber) {
        requireClaimed(token, cardNumber);
        return List.copyOf(chargesByCard.getOrDefault(cardNumber, List.of()));
    }

    public int balance(String token, String cardNumber) {
        return balanceOf(token, cardNumber);
    }

    public List<Charge> statement(String token, String cardNumber) {
        return chargesOf(token, cardNumber);
    }

    public void registerMerchant(String id, String privateCredential) {
        hasText(id, "Merchant id vacío");
        hasText(privateCredential, "Credencial vacía");
        Merchant prev = merchantsById.putIfAbsent(id, new Merchant(id, privateCredential));
        isTrue(prev == null, "Merchant ya registrado");
    }

    private Merchant requireMerchant(String merchantId, String privateCredential) {
        var merchant = merchantsById.get(merchantId);
        notNull(merchant, "Merchant desconocido");
        isTrue(merchant.privateCredential().equals(privateCredential), "Credencial inválida");
        return merchant;
    }

    public void charge(String token, String merchantId, String merchantCredential, String cardNumber, int amount, String description) {
        requireActiveSession(token);
        var merchant = requireMerchant(merchantId, merchantCredential);
        var card = requireClaimed(token, cardNumber);
        card.charge(amount, description);
        var charge = new Charge(card.cardNumber(), merchant.id(), amount, description, Instant.now(clock));
        chargesByCard.computeIfAbsent(cardNumber, k -> new ArrayList<>()).add(charge);
    }
}
