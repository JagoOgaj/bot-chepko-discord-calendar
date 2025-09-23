package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.calendar.repository.ServerUpdateAlertRepositoryImpl;

import java.util.Objects;

public class DisableUpdateAlertsMiddleware implements SlashCommandMiddleware {

    private final ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository;

    public DisableUpdateAlertsMiddleware(
            ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository) {
        this.serverUpdateAlertRepository = serverUpdateAlertRepository;
    }

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        if (this.serverUpdateAlertRepository
                .findById(Objects.requireNonNull(event.getGuild()).getIdLong())
                .isEmpty())
            throw new Exception("Cette fonctionnalité n’était pas activée sur ce serveur");
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {}
}
