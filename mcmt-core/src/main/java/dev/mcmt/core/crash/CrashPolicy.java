package dev.mcmt.core.crash;

/**
 * Defines how the mod should react to crashes.
 * Placeholder for future expansion.
 */
public enum CrashPolicy {
    IGNORE,     // Ignore the crash and try to continue
    LOG_ONLY,   // Log the crash but continue running
    STOP_SERVER // Stop the server or game on crash (not enacted here)
}
