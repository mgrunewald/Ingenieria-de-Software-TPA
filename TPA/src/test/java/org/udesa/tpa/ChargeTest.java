package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.TestFixtures.*;

public class ChargeTest {

    @Test
    void createsValidCharge() {
        var c = new Charge("1", "mercado-pago", 300, "cafe de havanna", now());
        assertAll(
                () -> assertEquals("1", c.cardNumber()),
                () -> assertEquals("mercado-pago", c.merchantId()),
                () -> assertEquals(300, c.amount()),
                () -> assertEquals("cafe de havanna", c.description()),
                () -> assertEquals(now(), c.timestamp())
        );
    }

    @Test
    void differentTimestampsMeanDifferentCharges() {
        var c1 = new Charge("1", "mercado-pago", 300, "cafe", now());
        var c2 = new Charge("1", "mercado-pago", 300, "cafe", oneSecondLater());
        assertNotEquals(c1, c2);
    }

    @Test
    void canNotChargeAmountZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 0,   "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", -3,  "kiosco", now()))
        );
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void failsOnInvalidMerchantId(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", invalid, 300, "cafe", now()));
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void failsOnInvalidDescription(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 300, invalid, now()));
    }

    @Test
    void cardNumberMustBeOnlyDigits() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a",  "mp", 1000, "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a1", "mp", 1000, "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("-1", "mp", 1000, "kiosco", now()))
        );
    }
}
