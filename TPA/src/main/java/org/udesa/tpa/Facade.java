package org.udesa.tpa;

import java.util.HashMap;
import java.util.Map;

public class Facade {
    private final Map<String, String> users = new HashMap<>();

    public void register(String username, String password) {
        users.put(username, password); //creo la entrada en la la lista de (usuarios, contraseña)
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }
}