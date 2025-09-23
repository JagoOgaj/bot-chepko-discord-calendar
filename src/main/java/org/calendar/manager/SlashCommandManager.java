package org.calendar.manager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import org.calendar.commands.SlashCommandHandler;
import org.calendar.mapper.SlashCommandMapper;
import org.calendar.middleware.MiddlewareAction;
import org.calendar.middleware.SlashCommandMiddleware;
import org.calendar.ui.EmbedFactory;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

public class SlashCommandManager {

    private final Map<String, SlashCommandHandler> slashCommands;
    private final SlashCommandMapper mapper;

    public SlashCommandManager(
            Map<String, SlashCommandHandler> slashCommands, SlashCommandMapper mapper) {
        this.slashCommands = slashCommands;
        this.mapper = mapper;
    }

    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String eventName = event.getName();

        Collection<SlashCommandMiddleware> middlewares =
                this.slashCommands.get(eventName).getCommandMiddlewares();

        if (!this.checkCommandMiddleware(middlewares, SlashCommandMiddleware::before, event))
            return;

        SlashCommandHandler handler = this.slashCommands.get(eventName);

        if (handler != null) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                event.getHook()
                        .sendMessageEmbeds(
                                EmbedFactory.create(
                                                "Une erreur est survenue",
                                                e.getMessage(),
                                                Color.red,
                                                Map.of())
                                        .build())
                        .queue();
            }
        }

        this.checkCommandMiddleware(middlewares, SlashCommandMiddleware::after, event);
    }

    private boolean checkCommandMiddleware(
            Collection<SlashCommandMiddleware> middlewares,
            MiddlewareAction<GenericCommandInteractionEvent> action,
            GenericCommandInteractionEvent event) {

        for (SlashCommandMiddleware mw : middlewares) {
            try {
                action.process(mw, event);
            } catch (Exception e) {
                event.getHook()
                        .sendMessageEmbeds(
                                EmbedFactory.create(
                                                "Une erreur est survenue",
                                                e.getMessage(),
                                                Color.red,
                                                Map.of())
                                        .build())
                        .queue();
                return false;
            }
        }
        return true;
    }

    public void registerAllCommandsToJDA(JDA jda) {
        slashCommands
                .values()
                .forEach(
                        cmd -> {
                            jda.upsertCommand(this.mapper.toSlashCommandData(cmd.getCommandMeta()))
                                    .queue();
                        });
    }
}
