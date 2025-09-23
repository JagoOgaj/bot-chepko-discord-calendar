package org.calendar.commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.entity.ServerConfigNotificationEntity;
import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.repository.ServerConfigNotificationRepositoryImpl;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.*;

public class SetupNotificationSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository;
    private final SlashCommandMeta meta;

    public SetupNotificationSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository,
            SlashCommandMeta meta) {
        this.middlewares = middlewares;
        this.serverConfigNotificationRepository = serverConfigNotificationRepository;
        this.meta = meta;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();
        TextChannel channel =
                Objects.requireNonNull(event.getOption(this.meta.options().getFirst().getName()))
                        .getAsChannel()
                        .asTextChannel();
        int reminderMinutes =
                Objects.requireNonNull(event.getOption(this.meta.options().get(1).getName()))
                        .getAsInt();

        ServerConfigNotificationEntity config =
                new ServerConfigNotificationEntity(
                        serverId, channel.getIdLong(), reminderMinutes, true);

        Optional<ServerConfigNotificationEntity> existing =
                this.serverConfigNotificationRepository.findByServerId(serverId);
        if (existing.isPresent()) {
            this.serverConfigNotificationRepository.update(config);
        } else {
            this.serverConfigNotificationRepository.save(config);
        }

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        existing.isPresent()
                                                ? "La configuration de notification à été mise à jour"
                                                : "Notifications configurées sur ce serveur avec un rappel",
                                        "Rappel de la configuration",
                                        Color.green,
                                        Map.of(
                                                "Minutes avant chaque évenement",
                                                String.valueOf(reminderMinutes)))
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
