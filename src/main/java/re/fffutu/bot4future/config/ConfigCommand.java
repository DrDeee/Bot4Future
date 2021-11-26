package re.fffutu.bot4future.config;

import org.javacord.api.interaction.*;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.CommandManager;
import re.fffutu.bot4future.util.SubcommandManager;

import java.util.List;

public class ConfigCommand implements CommandManager.Command {
    private SubcommandManager subcommandManager = new SubcommandManager();

    public ConfigCommand() {
        subcommandManager.addHandler("roles", new RolesHandler());
    }

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("config")
                .setDescription("Verwalte die Einstellungen des Bots für diesen Server.")
                .addOption(new SlashCommandOptionBuilder()
                        .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                        .setName("roles")
                        .setDescription("Verwalte die Rollen für diesen Server.")
                        .addOption(new SlashCommandOptionBuilder()
                                .setName("admin")
                                .setDescription("Setze die Admin-Rolle, oder lasse sie dir anzeigen.")
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .addOption(SlashCommandOption.create(SlashCommandOptionType.ROLE,
                                        "rolle",
                                        "Die neue Admin-Rolle.",
                                        false))
                                .build())
                        .addOption(new SlashCommandOptionBuilder()
                                .setName("moderator")
                                .setDescription("Setze die Moderator-Rolle, oder lasse sie dir anzeigen.")
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .addOption(SlashCommandOption.create(SlashCommandOptionType.ROLE,
                                        "rolle",
                                        "Die neue Moderator-Rolle.",
                                        false))
                                .build())
                        .addOption(new SlashCommandOptionBuilder()
                                .setName("muted")
                                .setDescription("Setze die Mute-Rolle, oder lasse sie dir anzeigen.")
                                .setType(SlashCommandOptionType.SUB_COMMAND)
                                .addOption(SlashCommandOption.create(SlashCommandOptionType.ROLE,
                                        "rolle",
                                        "Die neue Mute-Rolle.",
                                        false))
                                .build())
                        .build());
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of();
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return event -> {
            subcommandManager.handle(event.getSlashCommandInteraction().getOptions().get(0),
                    event.getSlashCommandInteraction());
        };
    }
}
