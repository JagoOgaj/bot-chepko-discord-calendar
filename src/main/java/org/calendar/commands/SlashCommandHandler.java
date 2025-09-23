package org.calendar.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.meta.SlashCommandMeta;
import org.calendar.middleware.SlashCommandMiddleware;

import java.util.Collection;

public interface SlashCommandHandler {
    void handle(SlashCommandInteractionEvent event) throws Exception;

    SlashCommandMeta getCommandMeta();

    Collection<SlashCommandMiddleware> getCommandMiddlewares();
}
