package com.jimm.serverdoctor;

/**
 * Simple severity label for a recorded lag spike (for history display).
 */
public enum LagSpikeSeverity {

    MILD("Mild"),
    MODERATE("Moderate"),
    SEVERE("Severe");

    private final String label;

    LagSpikeSeverity(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static LagSpikeSeverity from(LagSpikeEvent spike, LagSpikeConfig config) {
        boolean severeTps = spike.tpsTriggered() && spike.tps() < config.getTpsDropThreshold() - 3.0;
        boolean severeMspt = spike.msptTriggered() && spike.mspt() > config.getMsptSpikeThreshold() * 1.25;
        if (severeTps || severeMspt || (spike.tpsTriggered() && spike.msptTriggered())) {
            return SEVERE;
        }
        if (spike.tpsTriggered() || spike.msptTriggered()) {
            return MODERATE;
        }
        return MILD;
    }
}
