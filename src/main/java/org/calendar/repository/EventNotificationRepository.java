package org.calendar.repository;

import org.calendar.entity.EventNotificationEntity;

import java.util.Optional;

public interface EventNotificationRepository
        extends GenericRepository<EventNotificationEntity, Integer> {
    void deleteByEventAndServer(int eventId, Long serverId);

    boolean isEventNotified(int eventId, long serverId);
}
