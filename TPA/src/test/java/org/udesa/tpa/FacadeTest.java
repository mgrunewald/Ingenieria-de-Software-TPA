package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    private Facade facade;

    @Test
    void test01UnregisteredUsernameIsMissing() {
        facade = new Facade();
        assertFalse(facade.exists("martina")); // todavia no registre mi usuario
    }

    @Test
    void test02RegisterUsernameCorrectly() {
        facade = new Facade();
        facade.register("martina", "12345678");
        assertTrue(facade.exists("martina"));
    }

}

