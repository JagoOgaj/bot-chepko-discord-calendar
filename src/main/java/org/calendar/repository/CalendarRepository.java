package org.calendar.repository;

import org.calendar.entity.CalendarEntity;

import java.util.Optional;

public interface CalendarRepository extends GenericRepository<CalendarEntity, Integer> {
    Optional<CalendarEntity> findByServerId(Long serverId);
}
