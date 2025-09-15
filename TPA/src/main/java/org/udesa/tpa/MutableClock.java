package org.udesa.tpa;
import java.time.*;

class MutableClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    MutableClock(Instant start, ZoneId zone) {
        this.instant = start;
        this.zone = zone;
    }
    void plus(Duration d) { this.instant = this.instant.plus(d); }

    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return new MutableClock(instant, zone); }
    @Override public Instant instant() { return instant; }
}

