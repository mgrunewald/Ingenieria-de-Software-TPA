package org.udesa.tpa;
import static org.udesa.tpa.Utils.*;

public record Merchant(String id, String privateCredential) {
    public Merchant {
        nonBlank(id, "id");
        nonBlank(privateCredential, "privateCredential");
    }
}