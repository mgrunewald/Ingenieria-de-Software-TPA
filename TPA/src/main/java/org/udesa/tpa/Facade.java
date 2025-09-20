package org.udesa.tpa;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.udesa.tpa.Utils.*;

public final class Facade {
    private final Clock clock;
    private final Duration ttl;

    private final Map<String, String> users;
    private final Map<String, GiftCard> giftCardsByNumber;
    private final Map<String, Merchant> merchantsById;

    private final Map<String, UserSession> sessionsByToken = new HashMap<>();
    private final Map<String, Set<String>> claimsByToken = new HashMap<>();
    private final Map<String, List<Charge>> chargesByCard = new HashMap<>();

    public static String WRONG_PASSWORD = "Password is incorrect";
    public static String NULL_OBJECT = "Object can not be null";
    public static String GIFT_CARD_DOES_NOT_BELONG_TO_USER = "The gift card does not belong to the user";
    public static String UNCLAIMED_CARD = "This gift card has not been claimed in this session";
    public static String CLAIMED_CARD = "This gift card has been claimed in this session";
    public static String UNKNOWN_CARD = "Gift card not found";
    public static String UNKNOWN_USER = "User not found";
    public static String UNKNOWN_MERCHANT = "Merchant not found";


    public Facade(Clock clock, Duration ttl, Map<String, String> users, Map<String, GiftCard> giftCardsByNumber, Map<String, Merchant> merchantsById) {
        this.clock = Objects.requireNonNull(clock, NULL_OBJECT);
        this.ttl = Objects.requireNonNull(ttl, NULL_OBJECT);
        this.users = new HashMap<>(Objects.requireNonNull(users));
        this.giftCardsByNumber = new HashMap<>(Objects.requireNonNull(giftCardsByNumber));
        this.merchantsById = new HashMap<>(Objects.requireNonNull(merchantsById));
    }

    public boolean exists(String username) {
        boolean doesExists = users.containsKey(username);
        ensure(doesExists, UNKNOWN_USER);
        return doesExists;
    }

    private String requirePassword(String username) {
        String pass = users.get(username);
        nonBlank(pass, "username key in map");
        return pass;
    }

    public String login(String username, String password) {
        exists(username);
        String realPass = requirePassword(username);
        ensure(Objects.equals(realPass, password), WRONG_PASSWORD);
        UserSession session = UserSession.issue(username, ttl, clock);
        sessionsByToken.put(session.token(), session);
        return session.token();
    }

    public boolean isSessionActive(String token) {
        return Optional.ofNullable(sessionsByToken.get(token))
                .map(s -> {
                    try { s.ensureActive(clock); return true; }
                    catch (IllegalArgumentException ex) { return false; }
                })
                .orElse(false);
    }

    private UserSession requireActiveSession(String token) {
        UserSession session = Optional.ofNullable(sessionsByToken.get(nonBlank(token, "token"))) // BORRAR OFNULLABLE Y AGREGAR IFS
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        session.ensureActive(clock);
        return session;
    }

    private GiftCard requireCard(String cardNumber) {
        return Optional.ofNullable(giftCardsByNumber.get(cardNumber))
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_CARD));
    }

    public void claim(String token, String cardNumber) {
        UserSession session = requireActiveSession(token);
        GiftCard card = requireCard(cardNumber);
        ensure(card.owner().equals(session.username()), GIFT_CARD_DOES_NOT_BELONG_TO_USER);
        boolean added = claimsByToken.computeIfAbsent(token, k -> new HashSet<>()).add(cardNumber);
        ensure(added, CLAIMED_CARD);
    }

    private GiftCard requireClaimed(String token, String cardNumber) {
        UserSession session = requireActiveSession(token);
        GiftCard card = requireCard(cardNumber);
        ensure(claimsByToken.getOrDefault(token, Set.of()).contains(cardNumber), UNCLAIMED_CARD);
        ensure(card.owner().equals(session.username()), GIFT_CARD_DOES_NOT_BELONG_TO_USER);
        return card;
    }

    private GiftCard requireClaimedByAnyUser(String cardNumber) {
        GiftCard card = Optional.ofNullable(giftCardsByNumber.get(cardNumber))
                .orElseThrow(() -> new IllegalArgumentException(NULL_OBJECT));
        ensure(claimsByToken.values().stream().anyMatch(set -> set.contains(cardNumber)), UNCLAIMED_CARD);
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

    private Merchant requireMerchant(String merchantId, String privateCredential) {
        Merchant merchant = Optional.ofNullable(merchantsById.get(merchantId))
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_MERCHANT));
        ensure(merchant.privateCredential().equals(privateCredential), "privateCredential");
        return merchant;
    }

    public void charge(String merchantId, String merchantCredential, String cardNumber, int amount, String description) {
        Merchant merchant = requireMerchant(merchantId, merchantCredential);
        GiftCard card = requireClaimedByAnyUser(cardNumber);
        card.charge(amount, description);
        Charge charge = new Charge(card.cardNumber(), merchant.id(), amount, description, Instant.now(clock));
        chargesByCard.computeIfAbsent(cardNumber, k -> new ArrayList<>()).add(charge);
    }
}

