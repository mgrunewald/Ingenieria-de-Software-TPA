
package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tpa.TestFixtures.*;

class SanityTest {
    @Test
    void test01projectRunsUnitTests() {
        var card = gcMartina1000();
        assertEquals(1000, card.balance());
    }
}
