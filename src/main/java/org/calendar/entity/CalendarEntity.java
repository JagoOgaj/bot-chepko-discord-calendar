package org.calendar.entity;

import java.time.LocalDateTime;

public record CalendarEntity(
        Integer calendarId, Long serverId, String icsUrl, String name, LocalDateTime lastUpdated) {}
