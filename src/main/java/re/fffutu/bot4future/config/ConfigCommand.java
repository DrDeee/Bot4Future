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
        subcommandManager.addHandler("channels", new ChannelHandler());
    }

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("config")
                .setDescription("Verwalte die Einstellungen des Bots für diesen Server.")
                .addOption(RolesHandler.getSubcommand())
                .addOption(ChannelHandler.getSubcommand());
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
