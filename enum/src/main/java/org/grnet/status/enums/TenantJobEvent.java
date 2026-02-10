package org.grnet.status.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of all tenant status jobs.
 */
public enum TenantJobEvent {

    // Automatic jobs (updated by automation)
    INIT_AMS("i" +
            "nit_ams", EventMode.AUTO, Set.of(TenantJobProperty.TENANT_ID, TenantJobProperty.TENANT_NAME)),
    INIT_MONGO("init_mongo", EventMode.AUTO, Set.of(TenantJobProperty.TENANT_ID, TenantJobProperty.TENANT_NAME)),
    INIT_COMPUTE_ENGINE("init_compute_engine", EventMode.AUTO, Set.of(TenantJobProperty.TENANT_ID, TenantJobProperty.TENANT_NAME)),

    CHECK_READINESS("check_readiness", EventMode.AUTO, Set.of(TenantJobProperty.TENANT_ID, TenantJobProperty.TENANT_NAME)),

    // Manual jobs (completed by admin actions)

    CREATE_DOMAIN_NAMES("create_domain_names", EventMode.MANUAL, Set.of()), // no properties for now

    INIT_MONITORING_BOX("init_monitoring_box", EventMode.MANUAL, Set.of()), // no properties for now

    INIT_TOPOLOGY_CONNECTOR("init_topology_connector", EventMode.MANUAL,Set.of()),

    INIT_POEM("init_poem", EventMode.MANUAL,Set.of());// no properties for now

    private final String key;
    private final EventMode mode;
    private final Set<TenantJobProperty> allowedProperties;

    TenantJobEvent(String key, EventMode mode, Set<TenantJobProperty> allowedProperties) {
        this.key = key;
        this.mode = mode;
        this.allowedProperties = allowedProperties;
    }

    public String key() { return key; }
    public EventMode mode() { return mode; }

    public Set<TenantJobProperty> allowedProperties() { return allowedProperties; }

    /**
     * @return true if this job is intended to be completed manually by an admin
     */
    public boolean isManual() {
        return mode == EventMode.MANUAL;
    }
    public boolean isAuto() { return mode == EventMode.AUTO; }


    public static Optional<TenantJobEvent> fromKey(String key) {
        if (key == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(e -> e.key.equalsIgnoreCase(key))
                .findFirst();
    }

    /** Convenience for JSON ("auto"/"manual") */
    public String modeValue() {
        return mode.name().toLowerCase();
    }
}
