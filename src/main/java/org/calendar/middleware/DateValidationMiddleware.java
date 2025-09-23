package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.context.ContextKey;
import org.calendar.context.EventContext;
import org.calendar.exeptions.InvalidDateException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class DateValidationMiddleware implements SlashCommandMiddleware {

    private final String dateArg;
    private final EventContext context;

    public DateValidationMiddleware(String dateArg, EventContext context) {
        this.dateArg = dateArg;
        this.context = context;
    }

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        String dateStr = Objects.requireNonNull(event.getOption(this.dateArg)).getAsString();

        LocalDate date;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            date = LocalDate.parse(dateStr, formatter);
            this.context.put(ContextKey.DATE.name(), event.getId(), date);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException(
                    "Format invalide. Utilise **JJ/MM/AAAA** (ex: 10/09/2025)");
        }
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {
        String eventId = event.getId();
        if (this.context.eventContextExist(ContextKey.DATE.name(), eventId))
            this.context.clear(ContextKey.DATE.name(), eventId);
    }
}
