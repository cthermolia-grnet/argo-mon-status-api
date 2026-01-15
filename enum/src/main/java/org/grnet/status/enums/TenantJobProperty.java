package org.grnet.status.enums;

import java.util.Arrays;
import java.util.Optional;

public enum TenantJobProperty {

    TENANT_ID("tenant_id"),
    TENANT_NAME("tenant_name");

    private final String key;

    TenantJobProperty(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Optional<TenantJobProperty> fromKey(String raw) {
        if (raw == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(k -> k.key.equalsIgnoreCase(raw))
                .findFirst();
    }
}