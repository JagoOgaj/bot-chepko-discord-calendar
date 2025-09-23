package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.repository.ServerUpdateAlertRepositoryImpl;

import java.util.Objects;

public class EnableUpdateAlertsMiddleware implements SlashCommandMiddleware {
    private final ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository;

    public EnableUpdateAlertsMiddleware(
            ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository) {
        this.serverUpdateAlertRepository = serverUpdateAlertRepository;
    }

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        if (this.serverUpdateAlertRepository
                .findById(Objects.requireNonNull(event.getGuild()).getIdLong())
                .isPresent())
            throw new Exception("Les alertes de mise à jour sont déjà activées sur ce serveur.");
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {}
}
