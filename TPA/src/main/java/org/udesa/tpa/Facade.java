package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


public class Facade {
    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> sessions = new HashMap<>();

    public void register(String username, String password) {
        users.put(username, password); //creo la entrada en la la lista de (usuarios, contraseña)
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public String login(String username, String password) {
        String storedPassword = users.get(username);
        notNull(storedPassword, "Usuario no existe");
        isTrue(storedPassword.equals(password), "Constraseña incorrecta");
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }
}