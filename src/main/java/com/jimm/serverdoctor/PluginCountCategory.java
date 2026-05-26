package com.jimm.serverdoctor;

/**
 * Beginner-friendly label for how many plugins are installed.
 */
public enum PluginCountCategory {

    LIGHT("Light stack", "Fewer than 15 plugins"),
    MODERATE("Moderate stack", "15–29 plugins"),
    HEAVY("Heavy stack", "30–44 plugins"),
    VERY_LARGE("Very large stack", "45 or more plugins");

    private final String label;
    private final String description;

    PluginCountCategory(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public static PluginCountCategory fromCount(int pluginCount) {
        if (pluginCount < 15) {
            return LIGHT;
        }
        if (pluginCount < 30) {
            return MODERATE;
        }
        if (pluginCount < 45) {
            return HEAVY;
        }
        return VERY_LARGE;
    }
}
