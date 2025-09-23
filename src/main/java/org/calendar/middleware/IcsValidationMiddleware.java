package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.services.CalendarService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IcsValidationMiddleware implements SlashCommandMiddleware {
    private final String urlIcsArg;
    private final CalendarService calendarService;
    private final EventContext context;

    public IcsValidationMiddleware(
            String urlIcsArg, CalendarService calendarService, EventContext context) {
        this.urlIcsArg = urlIcsArg;
        this.calendarService = calendarService;
        this.context = context;
    }

    @Override
    public void before(@NotNull GenericCommandInteractionEvent event) throws Exception {
        this.context.put(
                ContextKey.CALENDAR.name(),
                event.getId(),
                this.calendarService.validateIcs(
                        Objects.requireNonNull(event.getOption(this.urlIcsArg)).getAsString()));
    }

    @Override
    public void after(@NotNull GenericCommandInteractionEvent event) throws Exception {
        String eventId = event.getId();
        if (this.context.eventContextExist(ContextKey.CALENDAR.name(), eventId))
            this.context.clear(ContextKey.CALENDAR.name(), eventId);
    }
}
