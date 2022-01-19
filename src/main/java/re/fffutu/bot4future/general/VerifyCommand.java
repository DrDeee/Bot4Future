package re.fffutu.bot4future.general;

import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.util.CommandManager.Command;
import re.fffutu.bot4future.util.CommandManager.CommandHandler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

public class VerifyCommand implements Command {
    @Override
    public SlashCommandBuilder getBuilder() {
        return new SlashCommandBuilder()
                .setName("verify")
                .setDescription("Verifiziere ein anderes Mitglied, und gebe ihm:ihr damit eine Rolle.")
                .setDefaultPermission(false)
                .addOption(SlashCommandOption.create(SlashCommandOptionType.USER, "mitglied",
                                                     "Das zu verifizierende Mitglied", true));
    }

    @Override
    public List<RoleType> getAllowed() {
        return List.of(RoleType.AUTO_ROLE);
    }

    @Override
    public CommandHandler getHandler() {
        return event -> {
            Server server = event.getSlashCommandInteraction().getServer().get();
            User user = event.getSlashCommandInteraction().getOptionUserValueByName("mitglied").get();
            if (user.isBot()) {
                event.getInteraction().createImmediateResponder()
                     .addEmbed(EmbedTemplate.error()
                                            .setDescription(
                                                    "Ich bezweifle, dass du den Bot privat kennst :)\n\nBitte " +
                                                            "verifiziere nur Personen, die du persönlich " +
                                                            "kennst, und nicht nur, weil du nett gefragt " +
                                                            "wurdest."))
                     .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                     .respond();
                return;
            }
            Optional<Role> optRole = Database.
                    ROLES.getRole(server.getId(),
                                  RoleType.GIVABLE_ROLE);
            if (optRole.isPresent()) {
                Role role = optRole.get();
                if (role.hasUser(user)) {
                    event.getInteraction().createImmediateResponder()
                         .addEmbed(EmbedTemplate.info()
                                                .setDescription(user.getMentionTag() + " ist bereits verifiziert und " +
                                                                        "hat die Rolle " + role.getMentionTag() + " " +
                                                                        "bereits."))
                         .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                         .respond();
                } else {
                    user.addRole(role).join();
                    event.getInteraction().createImmediateResponder()
                         .addEmbed(EmbedTemplate.success()
                                                .setDescription(
                                                        user.getMentionTag() + " wurde von dir verifiziert und " +
                                                                "hat die Rolle " + role.getMentionTag() + " " +
                                                                "bekommen."))
                         .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                         .respond();
                    try {
                        PrivateChannel channel = user.openPrivateChannel().join();
                        channel.sendMessage(EmbedTemplate.info()
                                                         .setDescription(
                                                                 "Du wurdest auf dem Server **" +
                                                                         server.getName() +
                                                                         "** von " +
                                                                         event.getInteraction()
                                                                              .getUser()
                                                                              .getMentionTag() +
                                                                         " verifiziert.\n\n Damit wurde dir eine " +
                                                                         "spezielle Rolle zugeteilt, mit der du " +
                                                                         "Zugriff auf weitere Bereiche des servers " +
                                                                         "bekommen hast."));
                    } catch (CompletionException e) {
                        // User does not allow private messages
                    }
                }
            } else event.getInteraction().createImmediateResponder()
                        .addEmbed(EmbedTemplate.error()
                                               .setDescription(
                                                       "Für diesen Server wurde noch keine Rolle festgelegt, welche " +
                                                               "verifizierten Mitgliedern zugeteilt werden soll. Bitte" +
                                                               " melde dich bei der Server-Administration, um diesen " +
                                                               "Fehler zu beheben."))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
        };
    }
}
