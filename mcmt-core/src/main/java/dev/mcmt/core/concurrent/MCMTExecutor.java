package dev.mcmt.core.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fixed-size (resizable) worker pool with named daemon threads
 * and simple helpers to run batches with timeouts and aggregated errors.
 */
public final class MCMTExecutor implements AutoCloseable {

    private volatile ThreadPoolExecutor pool;
    private final String threadNamePrefix;

    public MCMTExecutor(String threadNamePrefix, int threads) {
        if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");
        this.threadNamePrefix = Objects.requireNonNull(threadNamePrefix, "threadNamePrefix");
        this.pool = newPool(threads);
    }

    private ThreadPoolExecutor newPool(int threads) {
        ThreadFactory tf = new NamedThreadFactory(threadNamePrefix);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                tf,
                new ThreadPoolExecutor.AbortPolicy()
        );
        tpe.prestartAllCoreThreads();
        return tpe;
    }

    public synchronized void setThreadCount(int threads) {
        if (threads <= 0) throw new IllegalArgumentException("threads must be > 0");
        ThreadPoolExecutor old = this.pool;
        if (old.getCorePoolSize() == threads) return;

        ThreadPoolExecutor next = newPool(threads);
        old.shutdown();
        this.pool = next;
    }

    public int getThreadCount() {
        return pool.getCorePoolSize();
    }

    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    /**
     * Execute a batch of Runnables and wait for completion.
     * Aggregates thrown exceptions into a single ExecutionException with suppressed causes.
     */
    public void invokeParallel(List<? extends Runnable> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks.isEmpty()) return;
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        for (Runnable r : tasks) futures.add(pool.submit(r));
        aggregateJoin(futures, timeout, unit);
    }

    private static void aggregateJoin(List<Future<?>> futures, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        ExecutionException aggregate = null;
        for (Future<?> f : futures) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) throw new TimeoutException("Batch timed out");
            try {
                f.get(remaining, TimeUnit.NANOSECONDS);
            } catch (ExecutionException ee) {
                if (aggregate == null) {
                    aggregate = new ExecutionException("One or more tasks failed", ee.getCause());
                } else {
                    aggregate.addSuppressed(ee.getCause());
                }
            }
        }
        if (aggregate != null) throw aggregate;
    }

    @Override
    public void close() {
        ThreadPoolExecutor p = this.pool;
        p.shutdown();
        try {
            if (!p.awaitTermination(10, TimeUnit.SECONDS)) {
                p.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            p.shutdownNow();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger seq = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + seq.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
