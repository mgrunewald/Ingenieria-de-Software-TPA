package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        if (storedPassword == null) { throw new IllegalArgumentException("Usuario no existe"); }
        if (!storedPassword.equals(password)) { throw new IllegalArgumentException("Contraseña Incorrecta"); }
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);
        return token;
    }
}