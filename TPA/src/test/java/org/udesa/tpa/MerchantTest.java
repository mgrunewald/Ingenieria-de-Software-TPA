package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class MerchantTest {

    @Test
    void test01createsMerchantCorrectly() {
        var m = new Merchant("mercado-pago", "galperin123");
        assertAll(
                () -> assertEquals("mercado-pago", m.id()),
                () -> assertEquals("galperin123", m.privateCredential())
        );
    }

    @Test
    void test02failsMerchantWithInvalidId(){
        assertThrows(IllegalArgumentException.class, () -> new Merchant("", "cred"));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(" ", "cred"));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(null, "cred"));
    }

    @Test
    void test03failsMerchantWithInvalidPrivateCredential(){
        assertThrows(IllegalArgumentException.class, () -> new Merchant("mercado-pago", ""));
        assertThrows(IllegalArgumentException.class, () -> new Merchant("mercado-pago", " "));
        assertThrows(IllegalArgumentException.class, () -> new Merchant("mercado-pago", null));
    }

    @Test
    void test04createsDifferentMerchants() {
        var m1 = new Merchant("mercado-pago", "galperin123");
        var m2 = new Merchant("uala", "uala123");
        assertAll(
                () -> assertEquals("mercado-pago", m1.id()),
                () -> assertEquals("uala", m2.id()),
                () -> assertEquals("galperin123", m1.privateCredential()),
                () -> assertEquals("uala123", m2.privateCredential())
        );
    }
}
