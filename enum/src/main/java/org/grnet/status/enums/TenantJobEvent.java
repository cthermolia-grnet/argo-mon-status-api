package org.grnet.status.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Registry of all tenant status jobs.
 */
public enum TenantJobEvent {

    // Automatic jobs (updated by automation)
    INIT_AMS("init_ams", EventMode.AUTO),
    INIT_MONGO("init_mongo", EventMode.AUTO),

    // Manual jobs (completed by admin actions)
    CREATE_DOMAIN_NAMES("create_domain_names", EventMode.MANUAL);


    private final String key;
    private final EventMode mode;

    TenantJobEvent(String key, EventMode mode) {
        this.key = key;
        this.mode = mode;
    }

    public String key() { return key; }
    public EventMode mode() { return mode; }

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