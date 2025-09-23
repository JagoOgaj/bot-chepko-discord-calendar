package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.repository.ServerConfigNotificationRepositoryImpl;

import java.util.Objects;

public class ConfigExistenceMiddleware implements SlashCommandMiddleware {
    private final ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository;

    public ConfigExistenceMiddleware(
            ServerConfigNotificationRepositoryImpl serverConfigNotificationRepository) {
        this.serverConfigNotificationRepository = serverConfigNotificationRepository;
    }

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        if (this.serverConfigNotificationRepository
                .findByServerId(Objects.requireNonNull(event.getGuild()).getIdLong())
                .isEmpty())
            throw new Exception(
                    "Ce serveur n’a pas encore configuré les notifications d’événement, configurer avec `/set-up-event-alert` d'abord.");
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {}
}
