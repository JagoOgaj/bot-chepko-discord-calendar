package org.calendar.services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import org.calendar.entity.CalendarEntity;
import org.calendar.entity.EventEntity;
import org.calendar.entity.EventNotificationEntity;
import org.calendar.entity.ServerConfigNotificationEntity;
import org.calendar.repository.EventNotificationRepositoryImpl;
import org.calendar.repository.EventRepositoryImpl;
import org.calendar.repository.ServerConfigNotificationRepositoryImpl;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class NotificationService {
    private final ServerConfigNotificationRepositoryImpl serverRepo;
    private final EventRepositoryImpl eventRepo;
    private final EventNotificationRepositoryImpl eventNotificationRepo;
    private final CalendarService calendarService;
    private final JDA discordClient;
    private final ScheduledExecutorService scheduler;

    public NotificationService(
            ServerConfigNotificationRepositoryImpl serverRepo,
            EventRepositoryImpl eventRepo,
            EventNotificationRepositoryImpl eventNotificationRepo,
            CalendarService calendarService,
            JDA discordClient) {
        this.serverRepo = serverRepo;
        this.eventRepo = eventRepo;
        this.eventNotificationRepo = eventNotificationRepo;
        this.calendarService = calendarService;
        this.discordClient = discordClient;
        this.scheduler = Executors.newScheduledThreadPool(4);
    }

    public void scheduleTodayNotifications() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        serverRepo
                .findAllEnabled()
                .forEach(
                        server -> {
                            List<EventEntity> events;
                            try {
                                events =
                                        eventRepo
                                                .findByCalendarId(
                                                        calendarService
                                                                .getCalendarByServerId(
                                                                        server.serverId())
                                                                .calendarId())
                                                .stream()
                                                .filter(
                                                        e ->
                                                                !e.start_time().isBefore(startOfDay)
                                                                        && !e.start_time()
                                                                                .isAfter(endOfDay))
                                                .toList();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            for (EventEntity event : events) {
                                if (eventNotificationRepo.isEventNotified(
                                        event.eventId(), server.serverId())) continue;

                                LocalDateTime notificationTime =
                                        event.start_time().minusMinutes(server.reminderMinutes());
                                long delay = Duration.between(now, notificationTime).toMillis();

                                if (delay > 0) {
                                    scheduler.schedule(
                                            () -> sendNotification(event, server),
                                            delay,
                                            TimeUnit.MILLISECONDS);
                                } else {
                                    sendNotification(event, server);
                                }
                            }
                        });
    }

    private void sendNotification(EventEntity event, ServerConfigNotificationEntity server) {
        String time = event.start_time().toLocalTime() + " → " + event.end_time().toLocalTime();
        String desc =
                (event.description() == null || event.description().isBlank())
                        ? "Pas de description"
                        : event.description();
        String location =
                (event.location() == null || event.location().isBlank())
                        ? "Non précisé"
                        : event.location();

        String fieldValue =
                String.join(
                        "\n",
                        "- Heure : **" + time + "**",
                        "- Lieu : **" + location + "**",
                        "- Description : " + desc);

        EmbedBuilder embed =
                EmbedFactory.create(
                        String.format(
                                "Rappel d'événement dans %d minutes !", server.reminderMinutes()),
                        "",
                        Color.ORANGE,
                        Map.of());

        embed.addField(event.summary(), fieldValue, false);

        Objects.requireNonNull(discordClient.getTextChannelById(server.channelId()))
                .sendMessageEmbeds(embed.build())
                .queue();

        EventNotificationEntity notifEntity =
                new EventNotificationEntity(
                        event.eventId(),
                        server.serverId(),
                        LocalDateTime.now(ZoneId.of("Europe/Paris")));
        eventNotificationRepo.save(notifEntity);
        cleanOldNotifications();
    }

    public void cleanOldNotifications() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        List<EventNotificationEntity> allNotifs = eventNotificationRepo.findAll();

        for (EventNotificationEntity notif : allNotifs) {
            EventEntity event = eventRepo.findById(notif.eventId()).orElse(null);
            if (event != null && event.end_time().isBefore(now)) {
                eventNotificationRepo.deleteByEventAndServer(event.eventId(), notif.serverId());
            }
        }
    }

    public void sendUpdateAlerts(
            Integer calendarId, List<EventEntity> icsEvents, List<EventEntity> dbEvents)
            throws Exception {

        Optional<CalendarEntity> calendar = calendarService.getByCalendarId(calendarId);
        if (calendar.isEmpty()) throw new Exception("Pas de calendrier trouvé");

        ServerConfigNotificationEntity config =
                serverRepo.findByServerId(calendar.get().serverId()).orElse(null);
        if (config == null || !config.enabled()) return;

        Map<String, EventEntity> dbByUid =
                dbEvents.stream().collect(Collectors.toMap(EventEntity::uid, e -> e));
        Map<String, EventEntity> icsByUid =
                icsEvents.stream().collect(Collectors.toMap(EventEntity::uid, e -> e));

        List<String> changes = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (EventEntity icsEvent : icsEvents) {
            String icsTime =
                    icsEvent.start_time().format(formatter)
                            + " → "
                            + icsEvent.end_time().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (!dbByUid.containsKey(icsEvent.uid())) {
                changes.add("Nouvel événement : **" + icsEvent.summary() + " (" + icsTime + ")**");
            }
        }

        for (EventEntity icsEvent : icsEvents) {
            EventEntity dbEvent = dbByUid.get(icsEvent.uid());
            if (dbEvent != null
                    && (!Objects.equals(icsEvent.start_time(), dbEvent.start_time())
                            || !Objects.equals(icsEvent.end_time(), dbEvent.end_time())
                            || !Objects.equals(icsEvent.summary(), dbEvent.summary())
                            || !Objects.equals(icsEvent.description(), dbEvent.description())
                            || !Objects.equals(icsEvent.location(), dbEvent.location()))) {

                String icsTime =
                        icsEvent.start_time().format(formatter)
                                + " → "
                                + icsEvent.end_time().format(DateTimeFormatter.ofPattern("HH:mm"));
                changes.add("Modifié : **" + icsEvent.summary() + " (" + icsTime + ")**");
            }
        }

        for (EventEntity dbEvent : dbEvents) {
            if (!icsByUid.containsKey(dbEvent.uid())) {
                String dbTime =
                        dbEvent.start_time().format(formatter)
                                + " → "
                                + dbEvent.end_time().format(DateTimeFormatter.ofPattern("HH:mm"));
                changes.add("Supprimé : **" + dbEvent.summary() + " (" + dbTime + ")**");
            }
        }

        if (!changes.isEmpty()) {
            EmbedBuilder embed =
                    EmbedFactory.create(
                            "Mise à jour du calendrier",
                            "Des changements ont été détectés",
                            Color.BLUE,
                            Map.of("Changements", String.join("\n", changes)));

            Objects.requireNonNull(discordClient.getTextChannelById(config.channelId()))
                    .sendMessageEmbeds(embed.build())
                    .queue();
        }
    }
}
