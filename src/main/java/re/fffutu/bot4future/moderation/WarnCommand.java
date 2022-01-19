package re.fffutu.bot4future.moderation;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.AuditStore;
import re.fffutu.bot4future.db.AuditStore.AuditEntry;
import re.fffutu.bot4future.db.AuditStore.AuditType;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.logging.AuditLogger;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;

import java.util.List;

public class WarnCommand implements Command {
    AuditStore store = Database.AUDIT;

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("warn")
                .setDescription("Verwarne einen User")
                .setDefaultPermission(false)
                .addOption(SlashCommandOption.create(SlashCommandOptionType.USER, "user",
                                                     "Den zu verwarnenden User", true))
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "grund",
                                                     "Der Inhalt der Verwarnung", true));
    }

    @Override
    public List<RoleType> getAllowed() {
        return List.of(RoleType.ADMINISTRATOR, RoleType.MODERATOR);
    }

    @Override
    public CommandHandler getHandler() {
        return event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            Server server = interaction.getServer().get();
            User user = interaction.getOptionUserValueByName("user").get();
            String reason = interaction.getOptionStringValueByName("grund").get();
            if (!server.canKickUser(interaction.getUser(), user)) {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.error()
                                                  .setDescription("Müsstest " + user.getMentionTag() + " kicken " +
                                                                          "können, um ihn verwarnen zu dürfen!"))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
                return;
            }
            AuditEntry entry = new AuditEntry();
            entry.type = AuditType.WARN;
            entry.reason = reason;
            entry.userId = user.getId();
            entry.creatorId = interaction.getUser().getId();
            entry.serverId = server.getId();
            store.addAuditEntry(entry);
            AuditLogger.logAudit(server, entry);
            user.openPrivateChannel().thenAccept(channel -> {
                channel.sendMessage(EmbedTemplate.auditMessage(entry, server));
            });
            interaction.createImmediateResponder()
                       .addEmbed(EmbedTemplate.success()
                                              .setDescription(
                                                      "Du hast " + user.getMentionTag() + " erfolgreich verwarnt."))
                       .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                       .respond();
        };
    }
}
