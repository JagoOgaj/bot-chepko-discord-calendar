package org.calendar.middleware;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.services.CalendarService;

import java.util.Objects;

public class SetupNotificationMiddleware implements SlashCommandMiddleware {
    private final String reminderMinutesField;
    private final String chanelIdField;
    private final CalendarService calendarService;

    public SetupNotificationMiddleware(
            String reminderMinutesField, String chanelIdField, CalendarService calendarService) {
        this.reminderMinutesField = reminderMinutesField;
        this.chanelIdField = chanelIdField;
        this.calendarService = calendarService;
    }

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        int reminderMinutes =
                Objects.requireNonNull(event.getOption(this.reminderMinutesField)).getAsInt();

        TextChannel textChannel =
                event.getOption(this.chanelIdField).getAsChannel().asTextChannel();
        if (textChannel == null) {
            throw new IllegalArgumentException(
                    "Le channel fourni est invalide ou n'existe pas sur ce serveur.");
        }

        if (!textChannel.canTalk()) {
            throw new IllegalArgumentException(
                    "Je n'ai pas la permission d'envoyer des messages dans ce channel.");
        }

        if (!textChannel
                .getGuild()
                .getSelfMember()
                .hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS)) {
            throw new IllegalArgumentException(
                    "Je n'ai pas la permission d'envoyer des embeds dans ce channel.");
        }

        if (reminderMinutes < 1 || reminderMinutes > 60) {
            throw new IllegalArgumentException(
                    "Le nombre de minutes doit être compris entre 1 et 60.");
        }

        if (calendarService.getCalendarByServerId(
                        Objects.requireNonNull(event.getGuild()).getIdLong())
                == null) {
            throw new IllegalStateException(
                    "Aucun calendrier trouvé pour ce serveur, impossible de configurer les notifications.");
        }
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {}
}
