package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChargeTest {
    @Test
    void test01CreatesValidCharge() {
        Instant now = Instant.now();
        Charge charge = new Charge("1", "mercado-pago", 300, "cafe de havanna", now);
        assertEquals("1", charge.cardNumber());
        assertEquals("mercado-pago", charge.merchantId());
        assertEquals(300, charge.amount());
        assertEquals("cafe de havanna", charge.description());
        assertEquals(now, charge.timestamp());
    }

    @Test
    void test02FailsToCreateChargeWithInvalidValues() {
        Instant now = Instant.parse(Instant.now().toString());
        assertThrows(IllegalArgumentException.class, () -> new Charge("-1", "mercado-pago", 300, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "", 300, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", -3, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", 300, "", now));
    }
}
