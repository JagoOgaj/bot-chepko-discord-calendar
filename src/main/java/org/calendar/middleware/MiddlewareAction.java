package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

@FunctionalInterface
public interface MiddlewareAction<T extends GenericCommandInteractionEvent> {
    void process(SlashCommandMiddleware middleware, T event) throws Exception;
}
