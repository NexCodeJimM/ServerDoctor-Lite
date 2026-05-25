package com.jimm.serverdoctor;

/**
 * Cached result of a SpigotMC version check.
 */
public final class UpdateCheckResult {

    public enum State {
        NOT_CHECKED,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        CHECK_FAILED
    }

    private final State state;
    private final String remoteVersion;
    private final String message;

    private UpdateCheckResult(State state, String remoteVersion, String message) {
        this.state = state;
        this.remoteVersion = remoteVersion;
        this.message = message;
    }

    public static UpdateCheckResult notChecked() {
        return new UpdateCheckResult(State.NOT_CHECKED, null, "No update check has been run yet.");
    }

    public static UpdateCheckResult upToDate(String remoteVersion) {
        return new UpdateCheckResult(State.UP_TO_DATE, remoteVersion, "You are running the latest known version.");
    }

    public static UpdateCheckResult updateAvailable(String remoteVersion) {
        return new UpdateCheckResult(
                State.UPDATE_AVAILABLE,
                remoteVersion,
                "A newer version is available on SpigotMC."
        );
    }

    public static UpdateCheckResult failed(String message) {
        return new UpdateCheckResult(State.CHECK_FAILED, null, message);
    }

    public State getState() {
        return state;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUpdateAvailable() {
        return state == State.UPDATE_AVAILABLE;
    }
}
