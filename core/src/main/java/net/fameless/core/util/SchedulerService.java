package net.fameless.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger("BungeeAFK/SchedulerService");

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "BungeeAFK-Scheduler");
        t.setDaemon(true);
        return t;
    });
    public static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public static void shutdown() {
        VIRTUAL_EXECUTOR.shutdown();
        try {
            logger.info("Awaiting shutdown of virtual executor...");
            if (!VIRTUAL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Virtual executor did not terminate in time — forcing shutdown");
                VIRTUAL_EXECUTOR.shutdownNow();
            } else {
                logger.info("Virtual executor shutdown completed successfully");
            }
        } catch (InterruptedException e) {
            logger.warn("Shutdown interrupted while waiting for virtual executor termination");
            Thread.currentThread().interrupt();
        }

        SCHEDULED_EXECUTOR.shutdown();
        try {
            logger.info("Awaiting shutdown of scheduled executor...");
            if (!SCHEDULED_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Scheduled executor did not terminate in time — forcing shutdown");
                SCHEDULED_EXECUTOR.shutdownNow();
            } else {
                logger.info("Scheduled executor shutdown completed successfully");
            }
        } catch (InterruptedException e) {
            logger.warn("Shutdown interrupted while waiting for scheduled executor termination");
            Thread.currentThread().interrupt();
        }
    }

}
