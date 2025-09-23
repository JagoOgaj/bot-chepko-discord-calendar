package org.calendar.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateAlertScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public void start(Runnable task) {
        scheduleDailyTask(task, 8, 0);
        scheduleDailyTask(task, 15, 0);
    }

    private void scheduleDailyTask(Runnable task, int hour, int minute) {
        ZoneId zone = ZoneId.of("Europe/Paris");
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (!now.isBefore(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        long delay = Duration.between(now, nextRun).toMillis();

        scheduler.scheduleAtFixedRate(
                task, delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
