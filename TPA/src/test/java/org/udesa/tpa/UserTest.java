package org.udesa.tpa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {
    @Test
    void test01CreatesUserCorrectly() {
        String username = "martina";
        String password = "12345678";
        User user = new User(username, password);
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
    }

}
