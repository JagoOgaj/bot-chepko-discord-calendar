package org.calendar.entity;

import java.time.LocalDateTime;

public record EventEntity(
        Integer eventId,
        Integer calendarId,
        String uid,
        String summary,
        String description,
        String location,
        LocalDateTime start_time,
        LocalDateTime end_time,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
