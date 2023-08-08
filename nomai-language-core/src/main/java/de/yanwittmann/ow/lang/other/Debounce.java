package de.yanwittmann.ow.lang.other;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Debounce {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicReference<ScheduledFuture<?>> lastTask = new AtomicReference<>();
    private final long delay;

    public Debounce(long delay, TimeUnit unit) {
        this.delay = unit.toMillis(delay);
    }

    public void submit(Runnable task) {
        ScheduledFuture<?> lastScheduled = lastTask.getAndSet(scheduler.schedule(task, delay, TimeUnit.MILLISECONDS));
        if (lastScheduled != null) {
            lastScheduled.cancel(true);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
