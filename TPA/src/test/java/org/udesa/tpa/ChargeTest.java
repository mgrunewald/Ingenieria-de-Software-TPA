package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.udesa.tpa.FacadeTest.*;
import static org.junit.jupiter.api.Assertions.*;

public class ChargeTest {

    private static Instant now() {return Instant.parse("2025-09-18T12:00:00Z");};

    @Test
    void test01createsValidCharge() {
        Charge charge = new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, CHARGE_DESCRIPTION, now());
        assertAll(
                () -> assertEquals(CARD_NUMBER_1, charge.cardNumber()),
                () -> assertEquals(MERCHANT_ID_1, charge.merchantId()),
                () -> assertEquals(300, charge.amount()),
                () -> assertEquals(CHARGE_DESCRIPTION, charge.description()),
                () -> assertEquals(now(), charge.timestamp())
        );
    }

    @Test
    void test02canNotChargeAmountZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 0,   CHARGE_DESCRIPTION, now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, MERCHANT_ID_1, -3,  CHARGE_DESCRIPTION, now()))
        );
    }

    @Test
    void test03failsOnInvalidMerchantId(){
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, "", 300, CHARGE_DESCRIPTION, now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, " ", 300, CHARGE_DESCRIPTION, now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, null, 300, CHARGE_DESCRIPTION, now()));
    }

    @Test
    void test04failsOnInvalidDescription(){
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, "", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, " ", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, null, now()));
    }

    @Test
    void test05cardNumberMustBeOnlyDigits() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a",  MERCHANT_ID_1, 1000, CHARGE_DESCRIPTION, now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a1", MERCHANT_ID_1, 1000, CHARGE_DESCRIPTION, now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("-1", MERCHANT_ID_1, 1000, CHARGE_DESCRIPTION, now()))
        );
    }

    @Test
    void test06differentTimestampsMeanDifferentCharges() {
        Charge charge1 = new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, CHARGE_DESCRIPTION, now());
        Charge charge2 = new Charge(CARD_NUMBER_1, MERCHANT_ID_1, 300, CHARGE_DESCRIPTION, now().plusSeconds(1));
        assertNotEquals(charge1, charge2);
    }
}
