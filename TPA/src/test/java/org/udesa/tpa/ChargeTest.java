package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

public class ChargeTest {

    private Instant now;

    @BeforeEach
    public void setUp() {
        now = Instant.now();
    }

    @Test
    void test01CreatesValidCharge() {
        Charge charge = new Charge("1", "mercado-pago", 300, "cafe de havanna", now);
        assertEquals("1", charge.cardNumber());
        assertEquals("mercado-pago", charge.merchantId());
        assertEquals(300, charge.amount());
        assertEquals("cafe de havanna", charge.description());
        assertEquals(now, charge.timestamp());
    }

    @Test
    void test02FailsToCreateChargeWithInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Charge("-1", "mercado-pago", 300, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "", 300, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", null, 300, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", -3, "cafe de havanna", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", 300, "", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", 300, null, now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", 300, "cafe de havanna", null));
    }

    @Test
    void test03CanNotChargeAmountZero() {
        assertThrows(IllegalArgumentException.class, () -> new Charge("1", "mercado-pago", 0, "kiosco", now));
    }

    @Test
    void test04CardNumberMustBeAStringMadeOfOnlyDigits() {
        assertThrows(IllegalArgumentException.class, () -> new Charge("a", "mercado-pago", 1000, "kiosco", now));
        assertThrows(IllegalArgumentException.class, () -> new Charge("a1", "mercado-pago", 1000, "kiosco", now));
    }

    @Test
    void test05DifferentTimestampsMeanDifferentCharges() {
        Charge charge1 = new Charge("1", "mercado-pago", 300, "cafe", Instant.parse("2025-09-16T00:00:00Z"));
        Charge charge2 = new Charge("1", "mercado-pago", 300, "cafe", Instant.parse("2025-09-16T00:00:01Z"));
        assertNotEquals(charge1, charge2);
    }
}
