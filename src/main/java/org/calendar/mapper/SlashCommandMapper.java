package org.calendar.mapper;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import org.calendar.meta.SlashCommandMeta;

public final class SlashCommandMapper {
    public SlashCommandData toSlashCommandData(SlashCommandMeta meta) {
        SlashCommandData data = Commands.slash(meta.commandName(), meta.commandDescription());
        if (meta.options() != null) data.addOptions(meta.options());
        return data;
    }
}
