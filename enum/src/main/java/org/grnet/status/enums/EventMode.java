package org.grnet.status.enums;

/**
 * Indicates how a tenant job is executed.
 */
public enum EventMode {
    MANUAL("manual"),
    AUTO("auto");

    private final String json;
    EventMode(String json) { this.json = json; }
    public String json() { return json; }
}
