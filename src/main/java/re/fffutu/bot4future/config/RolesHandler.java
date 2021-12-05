package re.fffutu.bot4future.config;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.SubcommandHandler;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class RolesHandler implements SubcommandHandler {
    private RoleStore store = new RoleStore();

    private static SlashCommandOption ROLE = SlashCommandOption.create(SlashCommandOptionType.ROLE,
            "rolle",
            "Die neue Rolle.",
            false);

    public static SlashCommandOption getSubcommand() {
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                .setName("roles")
                .setDescription("Verwalte die Rollen für diesen Server.")
                .addOption(new SlashCommandOptionBuilder()
                        .setName("admin")
                        .setDescription("Setze die Admin-Rolle, oder lasse sie dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(ROLE)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("moderator")
                        .setDescription("Setze die Moderator-Rolle, oder lasse sie dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(ROLE)
                        .build())
                .build();
    }

    @Override
    public void handle(List<SlashCommandInteractionOption> options, SlashCommandInteraction interaction) {
        SlashCommandInteractionOption option = options.get(0);

        RoleStore.RoleType type = null;
        switch (option.getName()) {
            case "admin": {
                type = RoleStore.RoleType.ADMINISTRATOR;
                break;
            }
            case "moderator": {
                type = RoleStore.RoleType.MODERATOR;
                break;
            }
        }
        if (option.getOptions().size() == 0) {
            Optional<Role> optRole = store.getRole(interaction.getServer().get().getId(), type);
            InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL);
            if (optRole.isPresent()) {
                responder.addEmbed(EmbedTemplate.base()
                                .setTitle("Server-Rolle: " + type.getName())
                                .addField("Rollen-Name", optRole.get().getName())
                                .addField("Rollen-ID", optRole.get().getId() + "")
                                .addField("Mitglieder", optRole.get().getUsers().size() + "")
                                .setColor(optRole.get().getColor().orElse(Color.GRAY)))
                        .respond();
            } else {
                responder.addEmbed(EmbedTemplate.info()
                                .setTitle("Server-Rolle: " + type.getName())
                                .setDescription("Diese Rolle wurde für diesen Server noch nicht definiert!" +
                                        " Gebe dafür noch die Rolle als Parameter an!"))
                        .respond();
            }
        } else {
            Role role = option.getOptions().get(0).getRoleValue().get();
            store.setRole(interaction.getServer().get().getId(), type, role.getId());
            interaction.createImmediateResponder()
                    .addEmbed(EmbedTemplate.base()
                            .setColor(role.getColor().orElse(Color.GRAY))
                            .setTitle("Server-Rolle gesetzt: " + type.getName())
                            .setDescription("Du hast die " + type.getName() + "-Rolle auf "
                                    + role.getName() + " (" + role.getId() + ") gesetzt.")
                            .addField("Rollen-Name", role.getName())
                            .addField("Rollen-ID", role.getId() + "")
                            .addField("Mitglieder", role.getUsers().size() + ""))
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .respond();
        }
    }
}
