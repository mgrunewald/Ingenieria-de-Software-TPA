package org.udesa.tpa;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.udesa.tpa.Utils.*;

/**
 * Fachada del sistema de Gift Cards.
 * - Sin if/switch en la lógica: usamos Utils.ensure / Optional.
 * - Sin frameworks en dominio.
 * - Mantiene el diseño “por sesión”: claim por token.
 * - Overload opcional de charge(...) sin token, alineado al enunciado.
 */
public final class Facade {
    private final Clock clock;
    private final Duration ttl;

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, UserSession> sessionsByToken = new HashMap<>();
    private final Map<String, GiftCard> giftCardsByNumber = new HashMap<>();
    private final Map<String, Set<String>> claimsByToken = new HashMap<>();
    private final Map<String, Merchant> merchantsById = new HashMap<>();
    private final Map<String, List<Charge>> chargesByCard = new HashMap<>();


    public static String NULL_CLOCK = "Clock can not be null";
    public static String NULL_TTL = "TTL can not be null";
    public static String REGISTERED = "Already registered";
    public static String WRONG_PASSWORD = "Password is incorrect";
    public static String NULL_OBJECT = "Object can not be null";
    public static String GIFT_CARD_DOES_NOT_BELONG_TO_USER = "The gift card does not belong to the user";
    public static String UNCLAIMED_CARD = "This gift card has not been claimed in this session";
    public static String CLAIMED_CARD = "This gift card has been claimed in this session";
    public static String UNKNOWN_MERCHANT = "merchant desconocido";

    public Facade(Clock clock, Duration ttl) {
        this.clock = Objects.requireNonNull(clock, NULL_CLOCK);
        this.ttl = Objects.requireNonNull(ttl, NULL_TTL);
    }



    public void register(String username, String password) {
        nonBlank(username, "username");
        nonBlank(password, "password");
        String prev = users.putIfAbsent(username, password);
        ensure(prev == null, REGISTERED);
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    private String requirePassword(String username) {
        var pass = users.get(username);
        nonBlank(pass, "username");
        return pass;
    }

    public String login(String username, String password) {
        String realPass = requirePassword(username);
        ensure(Objects.equals(realPass, password), WRONG_PASSWORD);
        var session = UserSession.issue(username, ttl, clock);
        sessionsByToken.put(session.token(), session);
        return session.token();
    }

    public boolean isSessionActive(String token) {
        // sin if: Optional + try/catch dentro del map
        return Optional.ofNullable(sessionsByToken.get(token))
                .map(s -> {
                    try { s.ensureActive(clock); return true; }
                    catch (IllegalArgumentException ex) { return false; }
                })
                .orElse(false);
    }

    private UserSession requireActiveSession(String token) {
        var session = Optional.ofNullable(sessionsByToken.get(nonBlank(token, "token")))
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        session.ensureActive(clock);
        return session;
    }



    public void preloadGiftCard(GiftCard card) {
        Objects.requireNonNull(card, NULL_OBJECT);
        nonBlank(card.cardNumber(), "cardNumber");
        nonBlank(card.owner(), "owner");
        GiftCard prev = giftCardsByNumber.putIfAbsent(card.cardNumber(), card);
        ensure(prev == null, REGISTERED);
    }

    public void claim(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = Optional.ofNullable(giftCardsByNumber.get(cardNumber))
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        ensure(card.owner().equals(session.username()), GIFT_CARD_DOES_NOT_BELONG_TO_USER);
        boolean added = claimsByToken.computeIfAbsent(token, k -> new HashSet<>()).add(cardNumber);
        ensure(added, CLAIMED_CARD);
    }

    private GiftCard requireClaimed(String token, String cardNumber) {
        var session = requireActiveSession(token);
        var card = Optional.ofNullable(giftCardsByNumber.get(cardNumber))
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        ensure(claimsByToken.getOrDefault(token, Set.of()).contains(cardNumber), UNCLAIMED_CARD);
        ensure(card.owner().equals(session.username()), GIFT_CARD_DOES_NOT_BELONG_TO_USER);
        return card;
    }

    private GiftCard requireClaimedByAnyUser(String cardNumber) {
        var card = Optional.ofNullable(giftCardsByNumber.get(cardNumber))
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        ensure(
                claimsByToken.values().stream().anyMatch(set -> set.contains(cardNumber)),
                UNCLAIMED_CARD
        );
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


    public int balance(String token, String cardNumber) { return balanceOf(token, cardNumber); }
    public List<Charge> statement(String token, String cardNumber) { return chargesOf(token, cardNumber); }



    public void registerMerchant(String id, String privateCredential) {
        nonBlank(id, "id");
        nonBlank(privateCredential, "privateCredential");
        Merchant prev = merchantsById.putIfAbsent(id, new Merchant(id, privateCredential));
        ensure(prev == null, REGISTERED);
    }

    private Merchant requireMerchant(String merchantId, String privateCredential) {
        var merchant = Optional.ofNullable(merchantsById.get(merchantId))
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_MERCHANT));
        ensure(merchant.privateCredential().equals(privateCredential), "Credencial inválida");
        return merchant;
    }


    public void charge(String token, String merchantId, String merchantCredential,
                       String cardNumber, int amount, String description) {

        requireActiveSession(token);
        var merchant = requireMerchant(merchantId, merchantCredential);
        var card = requireClaimed(token, cardNumber);

        card.charge(amount, description); // valida monto/desc/saldo
        var charge = new Charge(card.cardNumber(), merchant.id(), amount, description, Instant.now(clock));
        chargesByCard.computeIfAbsent(cardNumber, k -> new ArrayList<>()).add(charge);
    }


    public void charge(String merchantId, String merchantCredential,
                       String cardNumber, int amount, String description) {

        var merchant = requireMerchant(merchantId, merchantCredential);
        var card = requireClaimedByAnyUser(cardNumber);

        card.charge(amount, description);
        var charge = new Charge(card.cardNumber(), merchant.id(), amount, description, Instant.now(clock));
        chargesByCard.computeIfAbsent(cardNumber, k -> new ArrayList<>()).add(charge);
    }
}

