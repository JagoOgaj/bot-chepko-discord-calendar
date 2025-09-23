package org.calendar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.repository.ServerUpdateAlertRepositoryImpl;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class DisableUpdateAlertSlashCommand implements SlashCommandHandler {

    private final Collection<SlashCommandMiddleware> middlewares;
    private final SlashCommandMeta meta;
    private final ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository;

    public DisableUpdateAlertSlashCommand(
            Collection<SlashCommandMiddleware> middlewares,
            SlashCommandMeta meta,
            ServerUpdateAlertRepositoryImpl serverUpdateAlertRepository) {
        this.middlewares = middlewares;
        this.meta = meta;
        this.serverUpdateAlertRepository = serverUpdateAlertRepository;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) throws Exception {
        this.serverUpdateAlertRepository.delete(
                Objects.requireNonNull(event.getGuild()).getIdLong());
        event.getHook()
                .sendMessageEmbeds(
                        EmbedFactory.create(
                                        "Fonctionnalité désactivé",
                                        "Les alertes de mise à jour de calendrier ont été désactivées.",
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
