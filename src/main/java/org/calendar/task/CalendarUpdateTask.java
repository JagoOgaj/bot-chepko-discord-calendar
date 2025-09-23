package org.calendar.task;

import org.calendar.entity.CalendarEntity;
import org.calendar.entity.EventEntity;
import org.calendar.entity.ServerUpdateAlertEntity;
import org.calendar.repository.EventRepositoryImpl;
import org.calendar.repository.ServerUpdateAlertRepositoryImpl;
import org.calendar.services.CalendarService;
import org.calendar.services.NotificationService;

import java.util.List;

public class CalendarUpdateTask implements Runnable {
    private final CalendarService calendarService;
    private final EventRepositoryImpl eventRepo;
    private final ServerUpdateAlertRepositoryImpl alertRepo;
    private final NotificationService notificationService;

    public CalendarUpdateTask(
            CalendarService calendarService,
            EventRepositoryImpl eventRepo,
            ServerUpdateAlertRepositoryImpl alertRepo,
            NotificationService notificationService) {
        this.calendarService = calendarService;
        this.eventRepo = eventRepo;
        this.alertRepo = alertRepo;
        this.notificationService = notificationService;
    }

    @Override
    public void run() {
        List<ServerUpdateAlertEntity> servers = alertRepo.findAll();

        for (ServerUpdateAlertEntity server : servers) {
            try {
                CalendarEntity calendar = calendarService.getCalendarByServerId(server.serverId());
                List<EventEntity> icsEvents = calendarService.fetchICSEvents(calendar.icsUrl());

                List<EventEntity> dbEvents = eventRepo.findByCalendarId(calendar.calendarId());

                calendarService.syncEvents(calendar.calendarId(), icsEvents, dbEvents);

                notificationService.sendUpdateAlerts(calendar.calendarId(), icsEvents, dbEvents);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
