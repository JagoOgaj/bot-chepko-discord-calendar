package org.calendar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.entity.CalendarEntity;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;

public class ShowCalendarSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final EventContext context;

    public ShowCalendarSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            EventContext context) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.context = context;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        String eventId = event.getId();

        CalendarEntity calendarEntity =
                (CalendarEntity) this.context.get(ContextKey.CALENDAR.name(), eventId);

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Ics enregistré",
                                        calendarEntity.icsUrl(),
                                        Color.green,
                                        null)
                                .build())
                .queue();
    }

    @Override
    public SlashCommandMeta getCommandMeta() {
        return this.meta;
    }

    @Override
    public Collection<SlashCommandMiddleware> getCommandMiddlewares() {
        return this.middlewares.isEmpty() ? Collections.emptyList() : this.middlewares;
    }
}
