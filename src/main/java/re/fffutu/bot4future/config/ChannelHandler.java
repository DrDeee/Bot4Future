package re.fffutu.bot4future.config;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.ChannelStore.ChannelType;
import re.fffutu.bot4future.util.SubcommandHandler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ChannelHandler implements SubcommandHandler {
    private ChannelStore store = new ChannelStore();

    public static SlashCommandOption getSubcommand() {
        SlashCommandOptionBuilder builder = new SlashCommandOptionBuilder()
                .setType(SlashCommandOptionType.SUB_COMMAND)
                .setName("channels")
                .setDescription("Verwalte die Channels für diesen Server.");

        SlashCommandOptionBuilder typeBuilder = new SlashCommandOptionBuilder()
                .setName("channel")
                .setDescription("Den Channel, den du verwalten willst.")
                .setRequired(true)
                .setType(SlashCommandOptionType.STRING);

        Arrays.stream(ChannelType.values())
              .sorted(Comparator.comparing(ChannelType::getName))
              .forEach(c -> typeBuilder.addChoice(c.getName(), c.getName()));

        builder.addOption(typeBuilder.build())
               .addOption(new SlashCommandOptionBuilder()
                                  .setName("neuerChannel")
                                  .setDescription("Der neue Channel")
                                  .setType(SlashCommandOptionType.CHANNEL)
                                  .setRequired(false)
                                  .build());
        return builder.build();
    }

    @Override
    public void handle(List<SlashCommandInteractionOption> options, SlashCommandInteraction interaction) {
        SlashCommandInteractionOption typeOption = options.get(0);

        ChannelType type = Arrays.stream(ChannelType.values())
                                 .filter(t -> t.getName()
                                               .equalsIgnoreCase(typeOption.getStringValue().get()))
                                 .findAny()
                                 .get();

        if (options.size() == 1) {
            Optional<ServerChannel> optChannel = store.getChannel(interaction.getServer().get().getId(), type);
            InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder()
                                                                       .setFlags(InteractionCallbackDataFlag.EPHEMERAL);
            if (optChannel.isPresent()) {
                responder.addEmbed(EmbedTemplate.info()
                                                .setTitle("Server-Channel: " + type.getName())
                                                .addField("Channel-Name",
                                                          optChannel.get().getName() + " (<#" +
                                                                  optChannel.get().getId() +
                                                                  ">")
                                                .addField("Channel-ID", optChannel.get().getId() + ""))
                         .respond();
            } else {
                responder.addEmbed(EmbedTemplate.info()
                                                .setTitle("Server-Channel: " + type.getName())
                                                .setDescription(
                                                        "Dieser Channel wurde für diesen Server noch nicht definiert!" +
                                                                " Gebe dafür noch den Channel als Parameter an!"))
                         .respond();
            }
        } else {
            ServerChannel channel = options.get(1).getChannelValue().get();
            if (!channel.asTextChannel().isPresent() && type.isTextChannel()) interaction.createImmediateResponder()
                                                                                         .addEmbed(EmbedTemplate.error()
                                                                                                                .setDescription(
                                                                                                                        "Du musst einen Text-Channel angeben!"))
                                                                                         .setFlags(
                                                                                                 InteractionCallbackDataFlag.EPHEMERAL)
                                                                                         .respond();
            else {
                store.setChannel(interaction.getServer().get().getId(), channel.getId(), type);
                interaction.createImmediateResponder()
                           .addEmbed(EmbedTemplate.info()
                                                  .setTitle("Server-Channel gesetzt: " + type.getName())
                                                  .setDescription("Du hast für diesen Server den Channel mit dem Typ " +
                                                                          type.getName() + " auf <#"
                                                                          + channel.getId() + "> gesetzt.")
                                                  .addField("Channel-Name", channel.getName())
                                                  .addField("Channel-ID", channel.getId() + ""))
                           .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                           .respond();
            }
        }
    }
}
