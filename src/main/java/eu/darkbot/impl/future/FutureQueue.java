package eu.darkbot.impl.future;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FutureQueue<T> implements Runnable {
    private static ScheduledExecutorService sharedExecutor;

    public FutureQueue(boolean shareThread) {
        ScheduledExecutorService executor = shareThread
                ? ((sharedExecutor == null) ? (sharedExecutor = Executors.newSingleThreadScheduledExecutor())
                : sharedExecutor) : Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(this, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {

    }
}
