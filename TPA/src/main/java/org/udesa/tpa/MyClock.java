package org.udesa.tpa;
import java.time.*;

class MyClock extends Clock {
    private Instant instant;

    MyClock(Instant start) {
        this.instant = start;
    }

    void plus(Duration d) { this.instant = this.instant.plus(d); }

    // si o si tengo que hacer esto para extenderme de Clock
    @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
    @Override public Clock withZone(ZoneId zone) { return new MyClock(instant); }
    @Override public Instant instant() { return instant; }
}
