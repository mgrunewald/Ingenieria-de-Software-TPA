package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChargeTest {
    @Test
    void test01CreatesValidCharge() {
        Instant now = Instant.parse("2020-09-20T00:00:00.00Z");
        Charge charge = new Charge("1", "mercado-pago", 300, "cafe de havanna", now);
        assertEquals("1", charge.cardNumber());
        assertEquals("mercado-pago", charge.merchantId());
        assertEquals(300, charge.amount());
        assertEquals("cafe de havanna", charge.description());
        assertEquals(now, charge.timestamp());
    }
}
