package org.calendar.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.calendar.manager.SlashCommandManager;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {

    private final SlashCommandManager cmdManager;

    public SlashCommandListener(SlashCommandManager cmdManager) {
        this.cmdManager = cmdManager;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        cmdManager.handle(event);
    }
}
