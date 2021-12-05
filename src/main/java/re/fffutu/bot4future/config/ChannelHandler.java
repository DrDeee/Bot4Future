package re.fffutu.bot4future.config;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.util.SubcommandHandler;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ChannelHandler implements SubcommandHandler {
    private ChannelStore store = new ChannelStore();

    private static SlashCommandOption CHANNEL = SlashCommandOption.create(SlashCommandOptionType.CHANNEL,
            "channel",
            "Der neue Channel.",
            false);

    public static SlashCommandOption getSubcommand() {
        return new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND_GROUP)
                .setName("channels")
                .setDescription("Verwalte die Channels für diesen Server.")
                .addOption(new SlashCommandOptionBuilder()
                        .setName("auditlog")
                        .setDescription("Setze den Audit-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("pm")
                        .setDescription("Setze den PM-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("messagelog")
                        .setDescription("Setze den Messagelog-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("serverlog")
                        .setDescription("Setze den Serverlog-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("userlog")
                        .setDescription("Setze den Userlog-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .addOption(new SlashCommandOptionBuilder()
                        .setName("store")
                        .setDescription("Setze den Store-Channel, oder lasse ihn dir anzeigen.")
                        .setType(SlashCommandOptionType.SUB_COMMAND)
                        .addOption(CHANNEL)
                        .build())
                .build();
    }

    @Override
    public void handle(List<SlashCommandInteractionOption> options, SlashCommandInteraction interaction) {
        SlashCommandInteractionOption option = options.get(0);

        ChannelStore.ChannelType type = null;
        switch (option.getName()) {
            case "auditlog": {
                type = ChannelStore.ChannelType.AUDIT;
                break;
            }
            case "pm": {
                type = ChannelStore.ChannelType.PM;
                break;
            }
            case "messagelog": {
                type = ChannelStore.ChannelType.MESSAGE_LOG;
                break;
            }
            case "userlog": {
                type = ChannelStore.ChannelType.USER_LOG;
                break;
            }
            case "serverlog": {
                type = ChannelStore.ChannelType.SERVER_LOG;
                break;
            }
            case "store": {
                type = ChannelStore.ChannelType.STORE;
                break;
            }
        }
        if (option.getOptions().size() == 0) {
            Optional<ServerChannel> optRole = store.getChannel(interaction.getServer().get().getId(), type);
            InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder()
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL);
            if (optRole.isPresent()) {
                responder.addEmbed(EmbedTemplate.info()
                                .setTitle("Server-Channel: " + type.getName())
                                .addField("Channel-Name", optRole.get().getName())
                                .addField("Channel-ID", optRole.get().getId() + ""))
                        .respond();
            } else {
                responder.addEmbed(EmbedTemplate.info()
                                .setTitle("Server-Channel: " + type.getName())
                                .setDescription("Dieser Channel wurde für diesen Server noch nicht definiert!" +
                                        " Gebe dafür noch den Channel als Parameter an!"))
                        .respond();
            }
        } else {
            ServerChannel channel = option.getOptions().get(0).getChannelValue().get();
            if (!channel.asTextChannel().isPresent() && type.isTextChannel()) interaction.createImmediateResponder()
                    .addEmbed(EmbedTemplate.error()
                            .setDescription("Du musst einen Text-Channel angeben!"))
                    .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                    .respond();
            else {
                store.setChannel(interaction.getServer().get().getId(), channel.getId(), type);
                interaction.createImmediateResponder()
                        .addEmbed(EmbedTemplate.info()
                                .setTitle("Server-Channel gesetzt: " + type.getName())
                                .setDescription("Du hast für diesen " + type.getName() + " gesetzt.")
                                .addField("Channel-Name", channel.getName())
                                .addField("Channel-ID", channel.getId() + ""))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
            }
        }
    }
}
