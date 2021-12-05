package re.fffutu.bot4future.moderation;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.AuditStore;
import re.fffutu.bot4future.db.AuditStore.AuditEntry;
import re.fffutu.bot4future.db.AuditStore.AuditType;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.logging.AuditLogger;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;
import re.fffutu.bot4future.util.TimeUtil;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MuteCommand implements Command {
    AuditStore auditStore = Database.AUDIT;
    RoleStore roleStore = Database.ROLES;

    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("mute")
                .setDescription("Mute ein Servermitglied für eine bestimmte Zeit.")
                .addOption(
                        SlashCommandOption.create(SlashCommandOptionType.USER, "user", "Der zu mutenden User", true))
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "grund", "Der Mute-Grund", true))
                .addOption(new SlashCommandOptionBuilder()
                                   .setName("minuten")
                                   .setDescription("Wie viele Minuten der User gemutet werden soll.")
                                   .setType(SlashCommandOptionType.LONG)
                                   .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                                   .setName("stunden")
                                   .setDescription("Wie viele Stunden der User gemutet werden soll.")
                                   .setType(SlashCommandOptionType.LONG)
                                   .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                                   .setName("tage")
                                   .setDescription("Wie viele Tage der User gemutet werden soll.")
                                   .setType(SlashCommandOptionType.LONG)
                                   .setRequired(false).build());
    }

    @Override
    public List<RoleType> getAllowed() {
        return List.of(RoleType.MODERATOR, RoleType.ADMINISTRATOR);
    }

    @Override
    public CommandHandler getHandler() {
        return event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            Server server = interaction.getServer().get();
            long millis = TimeUtil.getMillisFromInteraction(interaction);
            User user = interaction.getOptionUserValueByName("user").get();
            String reason = interaction.getOptionStringValueByName("grund").get();

            if (!server.canBanUser(interaction.getUser(), user)) {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.error()
                                                  .setDescription(
                                                          "Du musst die Berechtigung zum Bannen eines Users besitzen," +
                                                                  " um diesen zu muten."))
                           .respond();
                return;
            }

            if(millis == 0) {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.error()
                                                  .setDescription(
                                                          "Du kannst User nur temporär muten."))
                           .respond();
                return;
            }

            List<Role> userRoles = user.getRoles(server);

            AuditEntry entry = new AuditEntry();

            entry.type = AuditType.MUTE;
            entry.userId = user.getId();
            entry.reason = reason;
            entry.creatorId = interaction.getUser().getId();
            entry.expiresAt = Instant.now().toEpochMilli() + millis;
            entry.serverId = server.getId();
            entry.roles = userRoles.stream().map(Role::getId).collect(Collectors.toList());
            Optional<Role> muteRoleOpt = roleStore.getRole(server.getId(), RoleType.MUTED);
            if (muteRoleOpt.isPresent()) {
                if (muteRoleOpt.get().hasUser(user)) {
                    interaction.createImmediateResponder()
                               .addEmbed(EmbedTemplate.error()
                                                      .setDescription("Dieser User ist bereits gemutet."))
                               .respond();
                    return;
                }
                user.openPrivateChannel()
                    .thenAccept(pmChannel -> pmChannel.sendMessage(EmbedTemplate.auditMessage(entry, server)));
                AuditLogger.logAudit(server, entry);
                auditStore.addAuditEntry(entry);
                DiscordBot.INSTANCE.timedTaskManager.scheduleTask(entry);
                userRoles.forEach(user::removeRole);
                muteRoleOpt.get().addUser(user);
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.success()
                                                  .setDescription("Der User " + user.getDiscriminatedName()
                                                                          + " (" + user.getId() +
                                                                          ") wurde erfolgreich gebannt.")
                                                  .addField("Grund", entry.reason)
                                                  .addField("Dauer", millis == 0 ? "Permanent" :
                                                          "Ende <t:" + TimeUnit.MILLISECONDS.toSeconds(millis) + ":R>"))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
            } else {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.error()
                                                  .setDescription(
                                                          "Für diesen Server ist keine Mute-Rolle gesetzt. Nutze " +
                                                                  "dafür `/config roles muterole`."))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
                return;
            }
        };
    }
}
