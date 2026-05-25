package com.jimm.serverdoctor;

/**
 * Clamps config values to safe ranges so bad config.yml edits cannot break the plugin.
 */
public final class ConfigDefaults {

    private ConfigDefaults() {
    }

    public static int atLeast(int value, int minimum, int fallbackIfInvalid) {
        if (value < minimum) {
            return fallbackIfInvalid;
        }
        return value;
    }

    public static long atLeast(long value, long minimum, long fallbackIfInvalid) {
        if (value < minimum) {
            return fallbackIfInvalid;
        }
        return value;
    }

    public static int atLeastZero(int value) {
        return Math.max(0, value);
    }

    public static double clamp(double value, double minimum, double maximum, double fallback) {
        if (value < minimum || value > maximum || Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return value;
    }

    public static double percent(double value, double fallback) {
        return clamp(value, 1.0, 100.0, fallback);
    }

    public static String nonNullString(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
