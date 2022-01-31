package re.fffutu.bot4future.config;

import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.db.ServerStore;
import re.fffutu.bot4future.util.SubcommandHandler;

import java.util.List;

public class InviteCodeHandler implements SubcommandHandler {
    private static RoleStore roleStore = Database.ROLES;
    private static ServerStore serverStore = Database.SERVERS;

    public static SlashCommandOption getSubcommand() {
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND)
                .setName("invitecode")
                .setDescription("Verwalte die Einladung, bei welcher mensch die Auto-Rolle bekommt.")
                .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "code", "Der " +
                        "Invite-Code", false))
                .build();
    }

    @Override
    public void handle(List<SlashCommandInteractionOption> options, SlashCommandInteraction interaction) {
        if (options.size() == 1) {
            String code = options.get(0).getStringValue().get();
            if (interaction.getServer()
                           .get()
                           .getInvites()
                           .join()
                           .stream()
                           .filter(invite -> invite.getCode().equalsIgnoreCase(code)).findAny().isPresent()) {
                serverStore.setAttribute(interaction.getServer().get().getId(), ServerStore.INVITE_CODE, code);
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.success()
                                                  .setDescription(
                                                          "Der Einladungscode ist nun `" + code + "`. Gebe diesen" +
                                                                  " nur an Personen weiter, die berechtigt sind, " +
                                                                  "diesen zu nutzen!"))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
                DiscordBot.INSTANCE.inviteManager.setInvite(interaction.getServer().get(), code);
            } else interaction.createImmediateResponder()
                              .addEmbed(EmbedTemplate
                                                .error()
                                                .setDescription(
                                                        "Es exsistiert keine Einladung mit dem Code `" + code +
                                                                "`."))
                              .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                              .respond();
        } else {
            String code = serverStore.getAttribute(interaction.getServer().get().getId(), ServerStore.INVITE_CODE);
            if (code != null) {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate
                                             .info()
                                             .setTitle("Automatische Rollenvergabe: Einladungs-Code")
                                             .setDescription(
                                                     "Der Code für die automatische Rollenvergabe ist `" + code +
                                                             "`."))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
            } else {
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate
                                             .info()
                                             .setTitle("Automatische Rollenvergabe: Einladungs-Code")
                                             .setDescription(
                                                     ("Der Code für die automatische Rollenvergabe wurde noch " +
                                                             "nicht festgelegt.")))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
            }

        }
    }
}
