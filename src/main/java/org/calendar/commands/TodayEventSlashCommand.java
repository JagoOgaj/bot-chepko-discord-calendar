package org.calendar.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.entity.EventEntity;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.services.CalendarService;
import org.calendar.services.EventService;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class TodayEventSlashCommand implements SlashCommandHandler {
    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final EventService eventService;
    private final CalendarService calendarService;

    public TodayEventSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            EventService eventService,
            CalendarService calendarService) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.eventService = eventService;
        this.calendarService = calendarService;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {

        Long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();
        int calendarId = this.calendarService.getCalendarByServerId(serverId).calendarId();
        LocalDate todayParis = LocalDate.now(ZoneId.of("Europe/Paris"));
        LocalDateTime startOfDay = todayParis.atStartOfDay();
        LocalDateTime endOfDay = todayParis.atTime(23, 59, 59);

        List<EventEntity> events =
                this.eventService.getEventFor(calendarId, startOfDay).stream()
                        .filter(e -> !e.start_time().isAfter(endOfDay))
                        .toList();

        if (events.isEmpty()) {
            event.getHook()
                    .sendMessageEmbeds(
                            EmbedFactory.create(
                                            "Aucun résultat",
                                            "Aucun événement trouvé pour cette date",
                                            Color.red,
                                            Map.of("date", startOfDay.toString()))
                                    .build())
                    .queue();
            return;
        }

        EmbedBuilder embed =
                EmbedFactory.create("Événements du " + todayParis, "", Color.BLUE, Map.of());

        for (int i = 0; i < events.size(); i++) {
            String time =
                    events.get(i).start_time().toLocalTime()
                            + " → "
                            + events.get(i).end_time().toLocalTime();
            String desc =
                    (events.get(i).description() == null || events.get(i).description().isBlank())
                            ? "Pas de description"
                            : events.get(i).description();
            String location =
                    (events.get(i).location() == null || events.get(i).location().isBlank())
                            ? "Non précisé"
                            : events.get(i).location();

            // Affichage sous forme de liste avec les valeurs en gras
            String fieldValue =
                    String.join(
                            "\n",
                            "- Heure : **" + time + "**",
                            "- Lieu : **" + location + "**",
                            "- Description : " + desc,
                            i != events.size() - 1 ? "────────────────" : "");

            embed.addField(events.get(i).summary(), fieldValue, false);
        }

        event.getHook().sendMessageEmbeds(embed.build()).queue();
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
