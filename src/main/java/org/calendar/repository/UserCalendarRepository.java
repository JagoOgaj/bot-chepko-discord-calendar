package org.calendar.repository;

import org.calendar.entity.UserCalendarEntity;

import java.util.List;
import java.util.Optional;

public interface UserCalendarRepository extends GenericRepository<UserCalendarEntity, Integer> {
    Optional<UserCalendarEntity> findByUserAndCalendar(int userId, int calendarId);

    List<UserCalendarEntity> findByUserId(int userId);

    List<UserCalendarEntity> findByCalendarId(int calendarId);

    void deleteByUserAndCalendar(int userId, int calendarId);
}
