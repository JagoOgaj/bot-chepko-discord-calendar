package org.calendar.meta;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public record SlashCommandMeta(
        String commandName, String commandDescription, List<OptionData> options) {}
