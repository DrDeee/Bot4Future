package re.fffutu.bot4future.moderation;

import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.CommandManager;

import java.util.List;

public class BanCommand implements CommandManager.Command {
    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("ban")
                .setDefaultPermission(false)
                .setDescription("Banne einen User")
                .addOption(new SlashCommandOptionBuilder()
                        .setName("name")
                        .setDescription("Der zu bannende User")
                        .setType(SlashCommandOptionType.USER)
                        .setRequired(false)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("id")
                        .setDescription("Die ID des zu bannenden Users")
                        .setType(SlashCommandOptionType.STRING)
                        .setRequired(false)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("grund")
                        .setDescription("Der Grund für den Bann")
                        .setType(SlashCommandOptionType.STRING)
                        .setRequired(false)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("minuten")
                        .setDescription("Wie viele Minuten der User gebannt werden soll.")
                        .setType(SlashCommandOptionType.NUMBER)
                        .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("stunden")
                        .setDescription("Wie viele Stunden der User gebannt werden soll.")
                        .setType(SlashCommandOptionType.NUMBER)
                        .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("tage")
                        .setDescription("Wie viele Tage der User gebannt werden soll.")
                        .setType(SlashCommandOptionType.NUMBER)
                        .setRequired(false).build());
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of(RoleStore.RoleType.ADMINISTRATOR, RoleStore.RoleType.MODERATOR);
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return event -> {
            event.getSlashCommandInteraction().createImmediateResponder()
                    .addEmbed(EmbedTemplate.info().setDescription("Test"))
                    .respond();
        };
    }
}
