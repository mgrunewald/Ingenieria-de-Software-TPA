package org.udesa.tpa;

import java.time.Instant;
import java.util.stream.Stream;


public final class TestFixtures {
    private TestFixtures() {}

    public static Instant now() { return Instant.parse("2025-09-18T12:00:00Z"); }
    public static Instant oneSecondLater() { return now().plusSeconds(1); }

    public static GiftCard gcMartina1000() { return new GiftCard("martina", "1", 1_000); }
    public static GiftCard gcMartina100()  { return new GiftCard("martina", "2",   100); }

    public static Stream<String> blanks() { return Stream.of(null, "", "  "); }
}
