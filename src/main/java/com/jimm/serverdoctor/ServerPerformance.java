package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * Reads TPS and MSPT from Paper's {@link Server} API.
 */
public final class ServerPerformance {

    private ServerPerformance() {
    }

    public static double readCurrentTps() {
        double[] tpsSamples = Bukkit.getServer().getTPS();
        if (tpsSamples.length == 0) {
            return 20.0;
        }
        return tpsSamples[0];
    }

    public static double readMspt() {
        return Bukkit.getServer().getAverageTickTime();
    }
}
