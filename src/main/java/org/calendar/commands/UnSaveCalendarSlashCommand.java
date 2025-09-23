package org.calendar.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.services.CalendarService;
import org.calendar.services.UserCalendarService;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class UnSaveCalendarSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final UserCalendarService userCalendarService;
    private final CalendarService calendarService;

    public UnSaveCalendarSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            UserCalendarService userCalendarService,
            CalendarService calendarService) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.userCalendarService = userCalendarService;
        this.calendarService = calendarService;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        Long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();
        User user = event.getUser();
        int calendarId = this.calendarService.getCalendarByServerId(serverId).calendarId();
        this.userCalendarService.unsaveCalendar(user.getIdLong(), calendarId);

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Sauvegarde supprimé",
                                        "Le calendrier n'est plus sauvegardé pour le site",
                                        Color.green,
                                        Map.of())
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
