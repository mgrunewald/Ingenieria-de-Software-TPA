package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FacadeTest {
    private Facade facade;

    @Test
    void test01UnregisteredUsernameIsMissing() {
        facade = new Facade();
        assertFalse(facade.exists("martina"));
    }

    @Test
    void test02RegistersUsernameCorrectly() {
        facade = new Facade();
        facade.register("martina", "12345678");
        assertTrue(facade.exists("martina"));
    }

    @Test
    void test03FailsToLoginWithIncorrectPassword() {
        facade = new Facade();
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.login("martina", "otra"));
    }

    @Test
    void test04UnregisteredUserFailsToLogin() {
        Facade facade = new Facade();
        assertThrows(IllegalArgumentException.class, () -> facade.login("maximo", "password123"));
    }
}


