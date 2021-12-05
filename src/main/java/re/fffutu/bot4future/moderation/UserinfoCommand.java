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
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.CommandManager;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionException;

public class UserinfoCommand implements CommandManager.Command {
    private final AuditStore store = Database.AUDIT;

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("userinfo")
                .setDescription("Zeigt alle Informationen zu einem User an.")
                .addOption(SlashCommandOption.create(SlashCommandOptionType.USER,
                                                     "user", "Der User", false))
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "id", "Die ID des Users", false));
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of(RoleStore.RoleType.ADMINISTRATOR, RoleStore.RoleType.MODERATOR);
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();

            User user = null;

            if (interaction.getOptionByName("id").isPresent()) {
                try {
                    user = interaction.getApi().getUserById(interaction.getOptionStringValueByName("id").get()).join();
                } catch (CompletionException e) {
                    interaction.createImmediateResponder()
                               .addEmbed(EmbedTemplate.error()
                                                      .setDescription("Die angegebene User-ID ist nicht gültig."))
                               .respond();
                    return;
                }
            } else if (interaction.getOptionByName("user").isPresent()) {
                user = interaction.getOptionUserValueByName("user").get();
            }

            if (user == null) {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.error()
                                                  .setDescription("Du musst entweder eine User-ID oder den User " +
                                                                          "selbst angeben!"))
                           .respond();
                return;
            }

            Server server = interaction.getServer().get();
            String displayName = server.isMember(user) ? user.getDisplayName(server) : "Der User ist nicht auf " +
                    "diesem Server.";

            String joinedAt = server.isMember(user) ? (user.getJoinedAtTimestamp(server).isPresent() ?
                    "<t:" + user.getJoinedAtTimestamp(server).get().getEpochSecond() + ":R>"
                    : "Der Beitritts-Zeitpunkt ist nicht verfügbar.") :
                    "Der User ist nicht auf diesem Server.";

            List<AuditEntry> audits = store.getAuditEntries(server.getId(), user.getId());
            StringBuilder builder = new StringBuilder();
            if (audits.size() == 0) builder.append("Dieser User hat keine Audit-Einträge.");
            else
                audits.forEach(audit -> {
                    builder.append("**" + audit.type.asString().toUpperCase() + ":** *" + audit.reason + "*\n" +
                                           "**Status:** " +
                                           (audit.expiresAt == 0 ? "Permanent" :
                                                   ("<t:" + Instant.ofEpochMilli(audit.expiresAt).getEpochSecond() +
                                                           ":R>" +
                                                           (audit.expiresAt < System.currentTimeMillis() ? " beendet." :
                                                                   " auslaufend."))));
                    builder.append("\n **Erstellt <t:" + Instant.ofEpochMilli(audit.createdAt).getEpochSecond() + ":R" +
                                           "> von <@" + audit.creatorId + ">**\n\n");
                });
            interaction.createImmediateResponder()
                       .addEmbed(
                               EmbedTemplate.success()
                                            .setTitle("User: " + user.getDiscriminatedName())
                                            .addField("Name", user.getDiscriminatedName())
                                            .addField("Anzeige-Name:", displayName)
                                            .addField("ID", user.getIdAsString())
                                            .addField("Bot-Account", user.isBot() ? "Ja" : "Nein")
                                            .addField("Erstellt", "<t:" + user.getCreationTimestamp().getEpochSecond() +
                                                    ":R>")
                                            .addField("Beigetreten", joinedAt)
                                            .setThumbnail(user.getAvatar())
                       )
                       .addEmbed(EmbedTemplate.info()
                                              .setTitle("Audit-Einträge")
                                              .setDescription(builder.toString().trim()))
                       .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                       .respond();

        };
    }
}
