package org.calendar.repository;

import org.calendar.entity.ServerConfigNotificationEntity;

import java.util.List;
import java.util.Optional;

public interface ServerConfigNotificationRepository
        extends GenericRepository<ServerConfigNotificationEntity, Long> {
    Optional<ServerConfigNotificationEntity> findByServerId(Long serverId);

    List<ServerConfigNotificationEntity> findAllEnabled();
}
