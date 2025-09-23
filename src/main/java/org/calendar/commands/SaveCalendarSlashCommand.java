package org.calendar.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.entity.CalendarEntity;
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

public class SaveCalendarSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final UserCalendarService userCalendarService;
    private final CalendarService calendarService;

    public SaveCalendarSlashCommand(
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
        CalendarEntity calendar = this.calendarService.getCalendarByServerId(serverId);
        int calendarId = calendar.calendarId();
        this.userCalendarService.saveCalendar(user.getIdLong(), user.getName(), calendarId);

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Calendrier sauvegardé",
                                        String.format(
                                                "Tu as sauvegardé le calendrier `%s`, tu peux désormais accéder au calendrier via le site ...",
                                                calendar.name()),
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
