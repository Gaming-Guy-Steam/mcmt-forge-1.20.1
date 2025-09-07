package dev.mcmt.core;

/**
 * Thread-local tick context. Marks worker threads as "main-like" and carries metadata.
 */
public final class TickContext {
    public static final class Scope implements AutoCloseable {
        private final TickContext prev;
        private Scope(TickContext prev) { this.prev = prev; }
        @Override public void close() { LOCAL.set(prev); }
    }

    private static final ThreadLocal<TickContext> LOCAL = ThreadLocal.withInitial(TickContext::new);

    private long tick;
    private TaskDomain domain;
    private boolean mainLike;

    public static Scope enter(long tickNumber, TaskDomain domain, boolean mainLike) {
        TickContext prev = LOCAL.get();
        TickContext next = new TickContext();
        next.tick = tickNumber;
        next.domain = domain;
        next.mainLike = mainLike;
        LOCAL.set(next);
        return new Scope(prev);
    }

    public static long tickNumber() { return LOCAL.get().tick; }
    public static TaskDomain domain() { return LOCAL.get().domain; }
    public static boolean isMainLike() { return LOCAL.get().mainLike; }
}
