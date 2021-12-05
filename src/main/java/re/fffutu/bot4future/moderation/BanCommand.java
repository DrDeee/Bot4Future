package re.fffutu.bot4future.moderation;

import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.AuditStore;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.CommandManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements CommandManager.Command {
    AuditStore store = new AuditStore();

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
                        .setType(SlashCommandOptionType.INTEGER)
                        .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("stunden")
                        .setDescription("Wie viele Stunden der User gebannt werden soll.")
                        .setType(SlashCommandOptionType.INTEGER)
                        .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("tage")
                        .setDescription("Wie viele Tage der User gebannt werden soll.")
                        .setType(SlashCommandOptionType.INTEGER)
                        .setRequired(false).build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("löschen")
                        .setDescription("Von wie vielen Tagen der Bot die Nachrichten des zu" +
                                " Bannenden User gelöschen soll.")
                        .setType(SlashCommandOptionType.INTEGER)
                        .setRequired(false)
                        .build());
    }

    @Override
    public List<RoleStore.RoleType> getAllowed() {
        return List.of(RoleStore.RoleType.ADMINISTRATOR, RoleStore.RoleType.MODERATOR);
    }

    @Override
    public CommandManager.CommandHandler getHandler() {
        return event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            long targetId = 0;
            if (interaction.getOptionByName("id").isPresent())
                targetId = interaction.getOptionLongValueByName("id").get();
            if (interaction.getOptionByName("name").isPresent())
                targetId = interaction.getOptionUserValueByName("name").get().getId();

            User target = event.getApi().getUserById(targetId).join();
            if (target == null) {
                interaction.createImmediateResponder()
                        .addEmbed(EmbedTemplate.error()
                                .setDescription("Du musst entweder die User-ID oder den User angeben!"))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
                return;
            }
            if (interaction.getServer().get()
                    .getBans().join()
                    .stream().filter(ban -> ban.getUser() == target).count() != 0) {
                interaction.createImmediateResponder()
                        .addEmbed(EmbedTemplate.error().setDescription("Der User "
                                + target.getDiscriminatedName() + " wurde bereits gebannt!"))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
                return;
            }

            long minutes = interaction.getOptionLongValueByName("minuten").orElse(0L);
            long hours = interaction.getOptionLongValueByName("stunden").orElse(0L);
            long days = interaction.getOptionLongValueByName("tage").orElse(0L);

            long millis = TimeUnit.MINUTES.toMillis(minutes)
                    + TimeUnit.HOURS.toMillis(hours)
                    + TimeUnit.DAYS.toMillis(days);
            AuditStore.AuditEntry entry = new AuditStore.AuditEntry();
            entry.createdAt = System.currentTimeMillis();
            entry.type = AuditStore.AuditType.BAN;
            entry.expiresAt = millis;
            entry.creatorId = interaction.getUser().getId();
            entry.reason = interaction.getOptionStringValueByName("grund").orElse("~ kein Grund gegeben");
            entry.userId = targetId;
            store.addAuditEntry(interaction.getServer().get().getId(), target.getId(), entry);

            long delete = interaction.getOptionLongValueByName("löschen").orElse(0L);
            interaction.getServer().get().banUser(target, (int) delete, entry.reason);
            interaction.createImmediateResponder()
                    .addEmbed(EmbedTemplate.success()
                            .setDescription("Der User " + target.getDiscriminatedName()
                                    + " (" + target.getId() + ") wurde erfolgreich gebannt.")
                            .addField("Dauer", millis == 0 ? "Permanent" : "<t:" + TimeUnit.MILLISECONDS.toSeconds(millis) + ":R>"))
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .respond();
        };
    }
}
