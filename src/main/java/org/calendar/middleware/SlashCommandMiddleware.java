package org.calendar.middleware;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

public interface SlashCommandMiddleware {
    void before(GenericCommandInteractionEvent event) throws Exception;

    void after(GenericCommandInteractionEvent event) throws Exception;
}
