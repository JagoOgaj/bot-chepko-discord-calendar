package org.calendar.ui;

import net.dv8tion.jda.api.EmbedBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.Map;

public final class EmbedFactory {
    public static EmbedBuilder create(
            @NotNull String title,
            @NotNull String description,
            @NotNull Color color,
            @Nullable Map<String, String> fields) {
        EmbedBuilder builder =
                new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(color)
                        .setTimestamp(Instant.now())
                        .setFooter("Calendar Bot");

        if (fields != null) {
            fields.forEach((name, value) -> builder.addField(name, value, false));
        }
        return builder;
    }
}
