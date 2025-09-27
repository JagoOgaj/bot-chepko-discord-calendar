package org.calendar.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.entity.EventEntity;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.services.CalendarService;
import org.calendar.services.EventService;
import org.calendar.ui.EmbedFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class NextEventSlashCommand implements SlashCommandHandler {
    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final EventService eventService;
    private final CalendarService calendarService;
    private final EventContext context;

    public NextEventSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            EventService eventService,
            CalendarService calendarService,
            EventContext context) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.eventService = eventService;
        this.calendarService = calendarService;
        this.context = context;
    }

    @Override
    public void handle(@NotNull SlashCommandInteractionEvent event) throws Exception {
        String eventId = event.getId();
        Long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();
        LocalDate date = (LocalDate) this.context.get(ContextKey.DATE.name(), eventId);
        int calendarId = this.calendarService.getCalendarByServerId(serverId).calendarId();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
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

        EmbedBuilder embed = EmbedFactory.create("Événements du " + date, "", Color.BLUE, Map.of());

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
