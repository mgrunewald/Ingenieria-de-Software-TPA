package org.udesa.tpa;

import java.util.Objects;

public class GiftCard {
    public final Token token;

    public GiftCard(Token token) {
        this.token = Objects.requireNonNull(token);
    }

    public Token getToken() {
        return token;
    }
}
