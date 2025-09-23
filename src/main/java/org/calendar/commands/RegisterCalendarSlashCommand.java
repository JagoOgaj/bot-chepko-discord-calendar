package org.calendar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fortuna.ical4j.model.Calendar;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.entity.CalendarEntity;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.services.CalendarService;
import org.calendar.services.EventService;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RegisterCalendarSlashCommand implements SlashCommandHandler {
    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final CalendarService calendarService;
    private final EventContext context;
    private final EventService eventService;

    public RegisterCalendarSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            CalendarService calendarService,
            EventContext context,
            EventService eventService
            ) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.calendarService = calendarService;
        this.context = context;
        this.eventService = eventService;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        Long serverId = (Objects.requireNonNull(event.getGuild())).getIdLong();
        String icsUrlValue =
                Objects.requireNonNull(event.getOption(this.meta.options().getFirst().getName()))
                        .getAsString();
        String calendarName =
                Objects.requireNonNull(event.getOption(this.meta.options().get(1).getName()))
                        .getAsString();
        boolean alreadyRegistered = this.calendarService.exitsByServerId(serverId);
        boolean sameIcsUrl = this.calendarService.isSameIcsUrl(serverId, icsUrlValue);
        Calendar calendar = (Calendar) this.context.get(ContextKey.CALENDAR.name(), event.getId());
        CalendarEntity calendarEntity;

        if (alreadyRegistered) {
            if (sameIcsUrl) {
                event.getHook()
                        .sendMessageEmbeds(
                                EmbedFactory.create(
                                                "Une erreur est survenue",
                                                "Le lien ics fournis existe déjà pour ce serveur",
                                                Color.red,
                                                Map.of("Ics Url", icsUrlValue))
                                        .build())
                        .queue();
                return;
            } else {
                calendarEntity =
                        this.calendarService.updateCalendar(serverId, icsUrlValue, calendarName);
                event.getHook()
                        .sendMessageEmbeds(
                                EmbedFactory.create(
                                                "Mise à jour",
                                                "Le nouveau calendrier vas etre enregistré",
                                                Color.green,
                                                Map.of(
                                                        "Ics Url",
                                                        icsUrlValue,
                                                        "Calendar Name",
                                                        calendarName))
                                        .build())
                        .queue();

                this.eventService.deleteByCalendarId(
                        this.calendarService.getCalendarByServerId(serverId).calendarId()
                );
            }
        } else {
            this.calendarService.registerCalendar(serverId, icsUrlValue, calendarName);
            event.getHook()
                    .sendMessageEmbeds(
                            EmbedFactory.create(
                                            "Nouveau calendrier",
                                            "Un calendrier vas etre enregistré",
                                            Color.green,
                                            Map.of(
                                                    "Ics Url",
                                                    icsUrlValue,
                                                    "Calendar Name",
                                                    calendarName))
                                    .build())
                    .queue();
            calendarEntity = this.calendarService.getCalendarByServerId(serverId);
        }

        int nbEvents = calendarService.parseAndSaveEvents(calendar, calendarEntity);

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Calendrier enregistré ✅",
                                        String.format(
                                                "%d événements ont été ajoutés avec succès.",
                                                nbEvents),
                                        Color.green,
                                        Map.of(
                                                "Ics Url", icsUrlValue,
                                                "Calendar Name", calendarName,
                                                "Événements ajoutés", String.valueOf(nbEvents)))
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
