package dev.mcmt.core.crash;

/**
 * Listener interface for receiving crash events.
 */
@FunctionalInterface
public interface CrashListener {
    /**
     * Called when a crash is detected.
     *
     * @param throwable The exception or error that caused the crash.
     * @param policy    The crash policy in effect.
     */
    void onCrash(Throwable throwable, CrashPolicy policy);
}
