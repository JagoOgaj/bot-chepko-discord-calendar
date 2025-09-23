package org.calendar.middleware;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

public class PermissionMiddleware implements SlashCommandMiddleware {
    public PermissionMiddleware() {}

    @Override
    public void before(GenericCommandInteractionEvent event) throws Exception {
        Member member = event.getMember();
        if (member == null) {
            throw new Exception("Membre non reconnue");
        }
        if (!member.hasPermission(Permission.ADMINISTRATOR, Permission.MANAGE_PERMISSIONS)) {
            throw new Exception(
                    "Vous devez être administrateur ou avoir la permission de gérer le serveur pour exécuter cette commande.");
        }
    }

    @Override
    public void after(GenericCommandInteractionEvent event) throws Exception {}
}
