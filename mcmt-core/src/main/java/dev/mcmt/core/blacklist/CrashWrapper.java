package dev.mcmt.core.blacklist;

import dev.mcmt.core.crash.CrashListener;
import dev.mcmt.core.crash.CrashPolicy;

/**
 * Wraps task execution, blacklists on crash, and optionally notifies a listener.
 */
public final class CrashWrapper {
    private final BlacklistManager blacklist;
    private final CrashPolicy policy;
    private final CrashListener listener;

    public CrashWrapper(BlacklistManager blacklist) {
        this(blacklist, CrashPolicy.LOG_ONLY, null);
    }

    public CrashWrapper(BlacklistManager blacklist, CrashPolicy policy, CrashListener listener) {
        this.blacklist = blacklist;
        this.policy = policy != null ? policy : CrashPolicy.LOG_ONLY;
        this.listener = listener;
    }

    /**
     * Runs the task. If it throws, permanently blacklists the key and returns false.
     */
    public boolean runOrBlacklist(String key, Runnable task) {
        try {
            task.run();
            return true;
        } catch (Throwable t) {
            if (key != null && !key.isBlank()) {
                blacklist.blacklistPermanent(key, t);
            }
            if (listener != null) listener.onCrash(t, policy);
            // Policy STOP_SERVER is not enacted here; orchestration decides.
            return false;
        }
    }
}
