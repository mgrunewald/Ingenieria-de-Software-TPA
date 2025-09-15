package org.udesa.tpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Objects.isNull;
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
    void test03StartsSessionCorrectly() {
        facade = new Facade();
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678"); //arranca la sesion
        assertNotNull(token);
        assertTrue(facade.isSessionActive(token));
    }

    @Test
    void test04FailsToLoginWithIncorrectPassword(){
        facade = new Facade();
        facade.register("martina", "12345678");
        assertThrows(IllegalArgumentException.class, () -> facade.login("martina", "otra"));
    }

    @Test
    void test05UnregisteredUserFailsToLogin() {
        Facade facade = new Facade();
        assertThrows(IllegalArgumentException.class, () -> facade.login("maximo", "password123"));
    }

    @Test
    void test06TokenIsValid() {
        facade = new Facade();
        facade.register("martina", "12345678");
        String token = facade.login("martina", "12345678");
        assertFalse(isNull(token));
    }

}


