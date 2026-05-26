package com.jimm.serverdoctor;

/**
 * Whether Paper timings appear enabled (best-effort detection from config files).
 */
public enum PaperTimingsStatus {

    ENABLED("Enabled (detected in paper-global.yml)"),
    DISABLED("Disabled (detected in paper-global.yml)"),
    UNKNOWN("Unknown (config found but timings setting unclear)"),
    NOT_DETECTABLE("Not detectable (paper-global.yml not found)");

    private final String label;

    PaperTimingsStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean appearsEnabled() {
        return this == ENABLED;
    }
}
