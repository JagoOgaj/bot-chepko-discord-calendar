package org.calendar.entity;

public record ServerConfigNotificationEntity(
        Long serverId, Long channelId, int reminderMinutes, boolean enabled) {}
