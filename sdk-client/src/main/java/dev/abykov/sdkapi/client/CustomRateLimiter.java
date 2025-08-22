package dev.abykov.sdkapi.client;

import java.util.concurrent.*;

public class CustomRateLimiter {

    private final ScheduledExecutorService scheduler;
    private final Semaphore semaphore;
    private final int limit;
    private final long intervalMillis;

    public CustomRateLimiter(
            TimeUnit unit,
            int unitAmount,
            int limit,
            boolean fair
    ) {
        this.limit = limit;
        this.intervalMillis = unit.toMillis(unitAmount);
        this.semaphore = new Semaphore(limit, fair);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                () -> {
                    semaphore.drainPermits();
                    semaphore.release(limit);
                }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS
        );
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
