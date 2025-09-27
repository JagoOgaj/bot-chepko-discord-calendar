package org.calendar.services;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;

import org.calendar.entity.CalendarEntity;
import org.calendar.entity.EventEntity;
import org.calendar.exeptions.InvalidUrlIcsException;
import org.calendar.repository.CalendarRepositoryImpl;
import org.calendar.repository.EventRepositoryImpl;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarService {
    private final HttpClientService httpClientService;
    private final EventRepositoryImpl eventRepository;
    private final CalendarRepositoryImpl calendarRepository;

    public CalendarService(
            HttpClientService httpClientService,
            EventRepositoryImpl eventRepository,
            CalendarRepositoryImpl calendarRepository) {
        this.httpClientService = httpClientService;
        this.eventRepository = eventRepository;
        this.calendarRepository = calendarRepository;
    }

    public Calendar validateIcs(@NotNull String icsUrl) {
        try (InputStream in = httpClientService.get(icsUrl)) {
            Calendar calendar = new CalendarBuilder().build(in);
            if (calendar.getComponents().isEmpty()) {
                throw new InvalidUrlIcsException("Le calendrier ICS est vide.");
            }
            return calendar;
        } catch (Exception e) {
            throw new InvalidUrlIcsException("Impossible de récupérer ou parser le ICS.");
        }
    }

    public void registerCalendar(
            @NotNull Long serverId, @NotNull String icsUrl, @NotNull String name) {
        this.calendarRepository.save(
                new CalendarEntity(
                        null,
                        serverId,
                        icsUrl,
                        name,
                        LocalDateTime.now(ZoneId.of("Europe/Paris"))));
    }

    public CalendarEntity updateCalendar(
            @NotNull Long serverId, @NotNull String icsUrl, @NotNull String name)
            throws NoSuchElementException {
        CalendarEntity calendar = this.calendarRepository.findByServerId(serverId).orElseThrow();

        calendar =
                new CalendarEntity(
                        calendar.calendarId(),
                        serverId,
                        icsUrl,
                        name,
                        LocalDateTime.now(ZoneId.of("Europe/Paris")));

        this.calendarRepository.update(calendar);

        return calendar;
    }

    public int parseAndSaveEvents(
            @NotNull Calendar calendar, @NotNull CalendarEntity calendarEntity) throws Exception {

        calendar.getComponents("VEVENT")
                .forEach(
                        component -> {
                            EventEntity event =
                                    new EventEntity(
                                            null,
                                            calendarEntity.calendarId(),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.UID)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.UID),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.SUMMARY)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.SUMMARY),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.DESCRIPTION)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.DESCRIPTION),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.LOCATION)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.LOCATION),
                                            this.parseDate(
                                                    component
                                                            .getProperty(Property.DTSTART)
                                                            .map(Object::toString)
                                                            .orElse(null)),
                                            this.parseDate(
                                                    component
                                                            .getProperty(Property.DTEND)
                                                            .map(Object::toString)
                                                            .orElse(null)),
                                            LocalDateTime.now(ZoneId.of("Europe/Paris")),
                                            LocalDateTime.now(ZoneId.of("Europe/Paris")));
                            this.eventRepository.save(event);
                        });
        return calendar.getComponents("VEVENT").size();
    }

    private LocalDateTime parseDate(String value) {
        if (value == null) return null;

        value = value.trim();
        if (value.startsWith("DTSTART:")) value = value.substring("DTSTART:".length());
        if (value.startsWith("DTEND:")) value = value.substring("DTEND:".length());

        try {
            if (value.contains("T") && value.endsWith("Z")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX");
                OffsetDateTime odt = OffsetDateTime.parse(value, formatter);
                return odt.atZoneSameInstant(ZoneId.of("Europe/Paris")).toLocalDateTime();
            } else if (value.contains("T")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
                return LocalDateTime.parse(value, formatter);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                return LocalDate.parse(value, formatter).atStartOfDay();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cleanupValue(String value, String propertyKey) {
        if (value == null) return null;

        if (value.startsWith(propertyKey + ":")) {
            value = value.substring((propertyKey + ":").length());
        }

        value = value.replaceAll("[\\n\\r\\t]+", " ");

        value = value.replaceAll(" +", " ").trim();

        value = value.replace("\\n", " ");
        value = value.replace("\\,", " ");

        return value.trim();
    }

    public boolean exitsByServerId(@NotNull Long serverId) {
        return this.calendarRepository.findByServerId(serverId).isPresent();
    }

    public boolean isSameIcsUrl(@NotNull Long serverId, @NotNull String icsUrl) {
        Optional<CalendarEntity> calendarEntity = this.calendarRepository.findByServerId(serverId);
        if (calendarEntity.isPresent()) {
            return calendarEntity.get().icsUrl().equals(icsUrl);
        }
        return false;
    }

    public CalendarEntity getCalendarByServerId(@NotNull Long serverId) throws Exception {
        return this.calendarRepository
                .findByServerId(serverId)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Aucun calendrier trouvé pour ce serveur"));
    }

    public List<EventEntity> fetchICSEvents(@NotNull String icsUrl) throws Exception {
        Calendar icsCalendar = this.validateIcs(icsUrl);
        List<EventEntity> events = new ArrayList<>();

        icsCalendar
                .getComponents("VEVENT")
                .forEach(
                        component -> {
                            EventEntity event =
                                    new EventEntity(
                                            null,
                                            null,
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.UID)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.UID),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.SUMMARY)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.SUMMARY),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.DESCRIPTION)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.DESCRIPTION),
                                            this.cleanupValue(
                                                    component
                                                            .getProperty(Property.LOCATION)
                                                            .map(Object::toString)
                                                            .orElse(null),
                                                    Property.LOCATION),
                                            parseDate(
                                                    component
                                                            .getProperty(Property.DTSTART)
                                                            .map(Object::toString)
                                                            .orElse(null)),
                                            parseDate(
                                                    component
                                                            .getProperty(Property.DTEND)
                                                            .map(Object::toString)
                                                            .orElse(null)),
                                            LocalDateTime.now(ZoneId.of("Europe/Paris")),
                                            LocalDateTime.now(ZoneId.of("Europe/Paris")));
                            events.add(event);
                        });

        return events;
    }

    public void syncEvents(
            Integer calendarId, List<EventEntity> icsEvents, List<EventEntity> dbEvents) {

        Map<String, EventEntity> dbByUid =
                dbEvents.stream().collect(Collectors.toMap(EventEntity::uid, e -> e));

        Map<String, EventEntity> icsByUid =
                icsEvents.stream().collect(Collectors.toMap(EventEntity::uid, e -> e));

        for (EventEntity icsEvent : icsEvents) {
            EventEntity dbEvent = dbByUid.get(icsEvent.uid());
            if (dbEvent == null) {
                EventEntity newEvent =
                        new EventEntity(
                                null,
                                calendarId,
                                icsEvent.uid(),
                                icsEvent.summary(),
                                icsEvent.description(),
                                icsEvent.location(),
                                icsEvent.start_time(),
                                icsEvent.end_time(),
                                LocalDateTime.now(),
                                LocalDateTime.now());
                eventRepository.save(newEvent);
            } else if (!icsEvent.start_time().equals(dbEvent.start_time())
                    || !Objects.equals(icsEvent.summary(), dbEvent.summary())
                    || !Objects.equals(icsEvent.description(), dbEvent.description())
                    || !Objects.equals(icsEvent.location(), dbEvent.location())
                    || !icsEvent.end_time().equals(dbEvent.end_time())) {
                EventEntity updated =
                        new EventEntity(
                                dbEvent.eventId(),
                                dbEvent.calendarId(),
                                icsEvent.uid(),
                                icsEvent.summary(),
                                icsEvent.description(),
                                icsEvent.location(),
                                icsEvent.start_time(),
                                icsEvent.end_time(),
                                dbEvent.createdAt(),
                                LocalDateTime.now());
                eventRepository.update(updated);
            }
        }

        for (EventEntity dbEvent : dbEvents) {
            if (!icsByUid.containsKey(dbEvent.uid())
                    && dbEvent.start_time().isAfter(LocalDateTime.now())) {
                eventRepository.delete(dbEvent.eventId());
            }
        }
    }

    public Optional<CalendarEntity> getByCalendarId(Integer calendarId) {
        return this.calendarRepository.findById(calendarId);
    }
}
