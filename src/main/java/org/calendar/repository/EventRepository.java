package org.calendar.repository;

import org.calendar.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends GenericRepository<EventEntity, Integer> {

    List<EventEntity> findByCalendarId(int calendarId);

    List<EventEntity> findUpcomingEvents(int calendarId, LocalDateTime fromDate);

    void deleteByCalendarId(int calendarId);

}
