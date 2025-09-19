package org.udesa.tpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ChargeTest {

    private static Instant now() {return Instant.parse("2025-09-18T12:00:00Z");};

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

    @Test
    void test03failsOnInvalidMerchantId(){
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "", 300, "cafe", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", " ", 300, "cafe", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", null, 300, "cafe", now()));
    }

    @Test
    void test04failsOnInvalidDescription(){
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 300, "", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 300, " ", now()));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mp", 300, null, now()));
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
        var c2 = new Charge("1", "mercado-pago", 300, "cafe", now().plusSeconds(1));
        assertNotEquals(c1, c2);
    }
}
