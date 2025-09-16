package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MerchantTest {
    @Test
    void test01CreatesMerchantCorrectly() {
        Merchant merchant = new Merchant("mercado-pago", "galperin123");
        assertEquals("mercado-pago", merchant.id());
        assertEquals("galperin123", merchant.privateCredential());
    }

    @Test
    void test02FailsMerchantWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> new Merchant(null, "galperin123"));
        assertThrows(IllegalArgumentException.class, () -> new Merchant("", "galperin123"));
    }

    @Test
    void test03FailsMerchantWithInvalidPrivateCredential() {
        assertThrows(IllegalArgumentException.class, () -> new Merchant("mercado-pago", ""));
        assertThrows(IllegalArgumentException.class, () -> new Merchant("mercado-pago", null));
    }
}
