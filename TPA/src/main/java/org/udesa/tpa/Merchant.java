package org.udesa.tpa;
import static org.udesa.tpa.Utils.*;
import static org.udesa.tpa.Facade.*;

public record Merchant(String id, String privateCredential) {
    public Merchant {
        nonBlank(id, NULL_OR_EMPTY_VALUE);
        nonBlank(privateCredential, NULL_OR_EMPTY_VALUE);
    }
}