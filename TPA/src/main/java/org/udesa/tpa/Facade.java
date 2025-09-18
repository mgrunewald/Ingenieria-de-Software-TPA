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
        Utils.nonBlank(username, "username");
        Utils.nonBlank(password, "password");
        String prev = users.putIfAbsent(username, password);
        Utils.ensure(prev == null, "Usuario ya registrado" );
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    private String requirePassword(String username) { //con esto saco los ifs en login
        var pass = users.get(username);
        Utils.nonBlank(pass, "username");
        return pass;
    }

    public String login(String username, String password) {
        String realPass = requirePassword(username);
        Utils.ensure(Objects.equals(realPass, password), "Password incorrecto");
        var session = UserSession.issue(username, ttl, clock);
        sessionsByToken.put(session.token(), session);
        return session.token();
    }

    public boolean isSessionActive(String token) {
        var session = sessionsByToken.get(token);
        if (session == null) return false;
        try {
            session.ensureActive(clock);
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
        Utils.nonBlank(card.cardNumber(), "cardNumber");
        Utils.nonBlank(card.owner(), "owner");

        GiftCard prev = giftCardsByNumber.putIfAbsent(card.cardNumber(), card);
        Utils.ensure(prev == null, "Gift card ya registrado con ese número");
    }

    public void claim(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = giftCardsByNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        Utils.ensure(card.owner().equals(session.username()), "Gift card no pertenece al usuario");
        boolean added = claimsByToken.computeIfAbsent(token, k -> new HashSet<>()).add(cardNumber);
        Utils.ensure(added, "Gift card ya reclamada en esta session");
    }

    private GiftCard requireClaimed(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = giftCardsByNumber.get(cardNumber);
        notNull(card, "Gift card inexistente");
        Utils.ensure(claimsByToken.getOrDefault(token, Set.of()).contains(cardNumber), "La tarjeta no fue reclamada en esta sesión");
        Utils.ensure(card.owner().equals(session.username()), "Gift card no pertenece al usuario");
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
        Utils.nonBlank(id, "id");
        Utils.nonBlank(privateCredential, "privateCredential");
        Merchant prev = merchantsById.putIfAbsent(id, new Merchant(id, privateCredential));
        Utils.ensure(prev == null, "Merchant ya registrado");
    }

    private Merchant requireMerchant(String merchantId, String privateCredential) {
        var merchant = merchantsById.get(merchantId);
        notNull(merchant, "Merchant desconocido");
        Utils.ensure(merchant.privateCredential().equals(privateCredential), "Credencial inválida");
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
