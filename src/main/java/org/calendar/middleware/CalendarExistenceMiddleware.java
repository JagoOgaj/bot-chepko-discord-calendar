package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.exeptions.NoCalendarRegisteredException;
import org.calendar.services.CalendarService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CalendarExistenceMiddleware implements SlashCommandMiddleware {
    private final EventContext context;
    private final CalendarService calendarService;

    public CalendarExistenceMiddleware(EventContext context, CalendarService calendarService) {
        this.context = context;
        this.calendarService = calendarService;
    }

    @Override
    public void before(@NotNull GenericCommandInteractionEvent event) throws Exception {
        Long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();
        if (!this.calendarService.exitsByServerId(serverId)) {
            throw new NoCalendarRegisteredException("Aucun calendrier est enregistré");
        }
        this.context.put(
                ContextKey.CALENDAR.name(),
                event.getId(),
                this.calendarService.getCalendarByServerId(serverId));
    }

    @Override
    public void after(@NotNull GenericCommandInteractionEvent event) throws Exception {
        String eventId = event.getId();
        if (this.context.eventContextExist(ContextKey.CALENDAR.name(), eventId))
            this.context.clear(ContextKey.CALENDAR.name(), eventId);
    }
}
