package com.jimm.serverdoctor;

/**
 * Reads TPS and MSPT for Spigot/Paper 1.21.x through 26.2.x.
 */
public final class ServerPerformance {

    private ServerPerformance() {
    }

    public static double readCurrentTps() {
        return SpigotApiCompat.readCurrentTps();
    }

    public static double readMspt() {
        return SpigotApiCompat.readMspt();
    }
}
