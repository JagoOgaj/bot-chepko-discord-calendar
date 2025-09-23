package org.calendar.services;

import org.calendar.entity.EventEntity;
import org.calendar.repository.EventRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class EventService {
    private final EventRepositoryImpl eventRepository;

    public EventService(EventRepositoryImpl eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventEntity> getEventFor(int calendarId, LocalDateTime date) {
        return this.eventRepository.findUpcomingEvents(calendarId, date);
    }

    public void deleteByCalendarId(int calendarId) {
        this.eventRepository.deleteByCalendarId(calendarId);
    }
}
