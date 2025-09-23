package org.calendar.entity;

import java.time.LocalDateTime;

public record EventNotificationEntity(Integer eventId, Long serverId, LocalDateTime notifiedAt) {}
