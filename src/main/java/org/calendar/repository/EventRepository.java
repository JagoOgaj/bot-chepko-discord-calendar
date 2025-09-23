package org.calendar.repository;

import org.calendar.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends GenericRepository<EventEntity, Integer> {

    List<EventEntity> findByCalendarId(int calendarId);

    List<EventEntity> findUpcomingEvents(int calendarId, LocalDateTime fromDate);

    void deleteByCalendarId(int calendarId);

}
