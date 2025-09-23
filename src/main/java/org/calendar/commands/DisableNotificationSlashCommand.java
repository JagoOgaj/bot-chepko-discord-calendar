package org.calendar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.repository.EventNotificationRepositoryImpl;
import org.calendar.repository.ServerConfigNotificationRepositoryImpl;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class DisableNotificationSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final EventNotificationRepositoryImpl eventNotificationRepository;
    private final ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository;

    public DisableNotificationSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            EventNotificationRepositoryImpl eventNotificationRepository,
            ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.eventNotificationRepository = eventNotificationRepository;
        this.serverConfigNotificationRepository = serverConfigNotificationRepository;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        long serverId = Objects.requireNonNull(event.getGuild()).getIdLong();

        this.serverConfigNotificationRepository
                .findByServerId(serverId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Aucune configuration de notification trouvée pour ce serveur."));

        this.serverConfigNotificationRepository.delete(serverId);

        this.eventNotificationRepository.findAll().stream()
                .filter(n -> n.serverId() == serverId)
                .forEach(
                        n ->
                                this.eventNotificationRepository.deleteByEventAndServer(
                                        n.eventId(), serverId));

        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Notifications désactivées",
                                        "Toutes les notifications existantes supprimées et la fonctionnalité notification désactivé pour ce serveur",
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
