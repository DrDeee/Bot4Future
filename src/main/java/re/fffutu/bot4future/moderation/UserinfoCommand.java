package re.fffutu.bot4future.moderation;

import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.CommandManager;

import java.util.List;

public class UserinfoCommand implements CommandManager.Command {
    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("userinfo")
                .setDescription("Zeigt alle Informationen zu einem User an.")
                .addOption(SlashCommandOption.create(SlashCommandOptionType.USER, "user", "Der User", false))
                .addOption(SlashCommandOption.create(SlashCommandOptionType.INTEGER, "id", "Die ID des Users", false));
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of(RoleStore.RoleType.ADMINISTRATOR, RoleStore.RoleType.MODERATOR);
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return null;
    }
}
