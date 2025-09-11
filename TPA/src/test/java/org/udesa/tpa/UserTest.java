package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void usernameCannotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Username(" "));
    }

    @Test
    void passwordMustHaveAtLeast6Characters() {
        assertThrows(IllegalArgumentException.class, () -> new Password("123"));
    }

    @Test
    void createUserAndVerifyPassword() {
        Username username = new Username("titi");
        Password password = new Password("secret123");

        User user = new User(username, password);

        assertEquals(username, user.getUsername());
        assertTrue(user.correctPassword(new Password("secret123")));
        assertFalse(user.correctPassword(new Password("wrong123"))); // no coincide pero es válida
    }

}
