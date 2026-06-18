package com.jimm.serverdoctor;

/**
 * Whether a metric moved favorably, unfavorably, or stayed roughly the same vs a baseline.
 */
public enum BaselineMetricTrend {
    IMPROVED("improved"),
    STABLE("stable"),
    DEGRADED("degraded");

    private final String label;

    BaselineMetricTrend(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
