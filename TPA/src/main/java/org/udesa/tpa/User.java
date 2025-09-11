package org.udesa.tpa;

import java.util.Objects;

public class User {
    private final Username username;
    private final Password password;

    public User (Username username, Password password){
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public Username getUsername() {
        return  username;
    }

    public Password getPassword() {
        return  password;
    }

    public boolean correctPassword(Password rawPassword) {
        return this.password.equals(rawPassword);
    }
}


