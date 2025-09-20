package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.FacadeTest.*;
import static org.udesa.tpa.Facade.*;
public class MerchantTest {

    @Test
    void test01createsMerchantCorrectly() {
        Merchant merchant = new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        assertEquals(MERCHANT_ID_1, merchant.id());
        assertEquals(MERCHANT_CREDENTIAL_1, merchant.privateCredential());
    }

    @Test
    void test02failsMerchantWithInvalidId(){
        assertThrowsLike(() -> new Merchant("", MERCHANT_CREDENTIAL_1), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> new Merchant(" ", MERCHANT_CREDENTIAL_1), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> new Merchant(null, MERCHANT_CREDENTIAL_1), NULL_OR_EMPTY_VALUE);
    }

    @Test
    void test03failsMerchantWithInvalidPrivateCredential(){
        assertThrowsLike(() -> new Merchant(MERCHANT_ID_1, ""), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> new Merchant(MERCHANT_ID_1, " "), NULL_OR_EMPTY_VALUE);
        assertThrowsLike(() -> new Merchant(MERCHANT_ID_1, null), NULL_OR_EMPTY_VALUE);
    }

    @Test
    void test04createsDifferentMerchants() {
        Merchant merchant1 = new Merchant(MERCHANT_ID_1, MERCHANT_CREDENTIAL_1);
        Merchant merchant2 = new Merchant(MERCHANT_ID_2, MERCHANT_CREDENTIAL_2);
        assertEquals(MERCHANT_ID_1, merchant1.id());
        assertEquals(MERCHANT_ID_2, merchant2.id());
        assertEquals(MERCHANT_CREDENTIAL_1, merchant1.privateCredential());
        assertEquals(MERCHANT_CREDENTIAL_2, merchant2.privateCredential());
    }
}
