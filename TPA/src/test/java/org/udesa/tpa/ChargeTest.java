package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.TestFixtures.*;

public class ChargeTest {

    @Test
    void test01createsValidCharge() {
        var charge = new Charge("1", "mercado-pago", 300, "cafe de havanna", now());
        assertAll(
                () -> assertEquals("1", charge.cardNumber()),
                () -> assertEquals("mercado-pago", charge.merchantId()),
                () -> assertEquals(300, charge.amount()),
                () -> assertEquals("cafe de havanna", charge.description()),
                () -> assertEquals(now(), charge.timestamp())
        );
    }

    @Test
    void test02canNotChargeAmountZeroOrNegative() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 0,   "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", -3,  "kiosco", now()))
        );
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void test03failsOnInvalidMerchantId(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", invalid, 300, "cafe", now()));
    }

    @ParameterizedTest
    @MethodSource("org.udesa.tpa.TestFixtures#blanks")
    void test04failsOnInvalidDescription(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 300, invalid, now()));
    }

    @Test
    void test05cardNumberMustBeOnlyDigits() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a",  "mp", 1000, "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("a1", "mp", 1000, "kiosco", now())),
                () -> assertThrows(IllegalArgumentException.class, () -> new Charge("-1", "mp", 1000, "kiosco", now()))
        );
    }

    @Test
    void test06differentTimestampsMeanDifferentCharges() {
        var c1 = new Charge("1", "mercado-pago", 300, "cafe", now());
        var c2 = new Charge("1", "mercado-pago", 300, "cafe", oneSecondLater());
        assertNotEquals(c1, c2);
    }
}
