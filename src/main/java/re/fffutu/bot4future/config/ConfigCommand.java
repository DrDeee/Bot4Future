package re.fffutu.bot4future.config;

import org.javacord.api.interaction.*;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.util.CommandManager;
import re.fffutu.bot4future.util.SubcommandManager;

import java.util.List;

public class ConfigCommand implements CommandManager.Command {
    private SubcommandManager subcommandManager = new SubcommandManager();

    public ConfigCommand() {
        subcommandManager.addHandler("roles", new RolesHandler());
        subcommandManager.addHandler("channels", new ChannelHandler());
        subcommandManager.addHandler("invitecode", new InviteCodeHandler());
    }

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("config")
                .setDefaultPermission(false)
                .setDescription("Verwalte die Einstellungen des Bots für diesen Server.")
                .addOption(RolesHandler.getSubcommand())
                .addOption(ChannelHandler.getSubcommand())
                .addOption(InviteCodeHandler.getSubcommand());
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of(RoleType.ADMINISTRATOR);
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return event -> {
            LoggerFactory.getLogger("test").info("Heyho");
            subcommandManager.handle(event.getSlashCommandInteraction().getOptions().get(0),
                    event.getSlashCommandInteraction());
        };
    }
}
