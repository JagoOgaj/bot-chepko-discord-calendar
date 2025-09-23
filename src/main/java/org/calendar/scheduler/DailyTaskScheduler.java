package org.calendar.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyTaskScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(Runnable task) {
        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toSeconds(1);

        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
    }

    private long computeInitialDelay() {
        ZoneId parisZone = ZoneId.of("Europe/Paris");
        LocalDateTime now = LocalDateTime.now(parisZone);
        LocalDateTime nextRun = now.withHour(1).withMinute(0).withSecond(0);

        if (!now.isBefore(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        Duration duration = Duration.between(now, nextRun);
        return duration.getSeconds();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
