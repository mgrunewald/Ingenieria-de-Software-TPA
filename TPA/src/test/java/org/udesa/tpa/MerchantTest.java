package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.FacadeTest.*;
public class MerchantTest {

    @Test
    void test01createsMerchantCorrectly() {
        Merchant merchant = new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        assertAll(
                () -> assertEquals(MERCHANT_ID_1, merchant.id()),
                () -> assertEquals(MERCHANT_CREDENTIAL_1, merchant.privateCredential())
        );
    }

    @Test
    void test02failsMerchantWithInvalidId(){
        assertThrows(IllegalArgumentException.class, () -> new Merchant("", MERCHANT_CREDENTIAL_1));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(" ", MERCHANT_CREDENTIAL_1));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(null, MERCHANT_CREDENTIAL_1));
    }

    @Test
    void test03failsMerchantWithInvalidPrivateCredential(){
        assertThrows(IllegalArgumentException.class, () -> new Merchant(MERCHANT_ID_1, ""));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(MERCHANT_ID_1, " "));
        assertThrows(IllegalArgumentException.class, () -> new Merchant(MERCHANT_ID_1, null));
    }

    @Test
    void test04createsDifferentMerchants() {
        Merchant merchant1 = new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        Merchant merchant2 = new Merchant(MERCHANT_ID_2, MERCHANT_CREDENTIAL_2);
        assertAll(
                () -> assertEquals(MERCHANT_ID_1, merchant1.id()),
                () -> assertEquals(MERCHANT_ID_2, merchant2.id()),
                () -> assertEquals(MERCHANT_CREDENTIAL_1, merchant1.privateCredential()),
                () -> assertEquals(MERCHANT_CREDENTIAL_2, merchant2.privateCredential())
        );
    }
}
