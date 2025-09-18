package org.udesa.tpa;

public record Merchant(String id, String privateCredential) {
    public Merchant {
        Utils.nonBlank(id, "id");
        Utils.nonBlank(privateCredential, "privateCredential");
    }
}