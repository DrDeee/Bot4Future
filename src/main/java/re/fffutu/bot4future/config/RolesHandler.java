package re.fffutu.bot4future.config;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.util.SubcommandHandler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RolesHandler implements SubcommandHandler {
    private static SlashCommandOption ROLE = SlashCommandOption.create(SlashCommandOptionType.ROLE,
                                                                       "rolle",
                                                                       "Die neue Rolle.",
                                                                       false);
    private RoleStore store = Database.ROLES;

    public static SlashCommandOption getSubcommand() {
        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND)
                .setName("roles")
                .setDescription("Verwalte die Rollen für diesen Server.");
        SlashCommandOptionBuilder typeBuilder = new SlashCommandOptionBuilder()
                .setName("rolle")
                .setDescription("Die Rolle, die du verwalten willst.")
                .setType(SlashCommandOptionType.STRING)
                .setRequired(true);

        Arrays.stream(RoleType.values())
              .sorted(Comparator.comparing(RoleType::getName))
              .forEach(type -> typeBuilder.addChoice(type.getName(),
                                                     type.getName()));
        builder.addOption(typeBuilder.build())
               .addOption(SlashCommandOption
                                  .create(SlashCommandOptionType.ROLE,
                                          "neueRolle",
                                          "Die neue Rolle für diesen Typ.",
                                          false));
        return builder.build();
    }

    @Override
    public void handle(List<SlashCommandInteractionOption> options, SlashCommandInteraction interaction) {
        SlashCommandInteractionOption typeOption = options.get(0);


        RoleStore.RoleType type =
                Arrays.stream(RoleType.values())
                      .filter(t -> t.getName().equalsIgnoreCase(typeOption.getStringValue().get()))
                      .findAny()
                      .get();
        if (options.size() == 1) {
            Optional<Role> optRole = store.getRole(interaction.getServer().get().getId(), type);
            InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder()
                                                                       .setFlags(InteractionCallbackDataFlag.EPHEMERAL);
            if (optRole.isPresent()) {
                responder.addEmbed(EmbedTemplate.logRole(optRole.get())
                                                .setTitle("Server-Rolle: " + type.getName()))
                         .respond();
            } else {
                responder.addEmbed(EmbedTemplate.info()
                                                .setTitle("Server-Rolle: " + type.getName())
                                                .setDescription(
                                                        "Diese Rolle wurde für diesen Server noch nicht definiert!" +
                                                                " Gebe dafür noch die Rolle als Parameter an!"))
                         .respond();
            }
        } else {
            Role role = options.get(1).getRoleValue().get();
            if (role.isManaged()) {
                interaction.createImmediateResponder().addEmbed(EmbedTemplate.errorRoleManaged(role))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL).respond();
                return;
            }
            store.setRole(interaction.getServer().get().getId(), type, role.getId());
            interaction.createImmediateResponder()
                       .addEmbed(EmbedTemplate.logRole(role)
                                              .setTitle("Server-Rolle gesetzt: " + type.getName())
                                              .setDescription("Du hast die " + type.getNamePart() + "Rolle auf "
                                                                      + role.getName() + " (" + role.getId() +
                                                                      ") gesetzt."))
                       .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                       .respond();
        }
    }
}
