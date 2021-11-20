package re.fffutu.bot4future.logging.actions;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.logging.ExceptionLogger;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.logging.MessageStore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static re.fffutu.bot4future.logging.EventAuditLogButtonTemplates.*;

public class MessageDetailsActionListener implements ButtonClickListener {
    MessageStore store = new MessageStore();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        if ((parts.length != 4 && parts.length != 2) || !parts[0].equals("msg-details")) return;

        ButtonInteraction interaction = event.getButtonInteraction();
        switch (parts[1]) {
            case "open": {
                event.getButtonInteraction().createImmediateResponder().respond();
                long channelId = Long.parseLong(parts[2]);
                long msgId = Long.parseLong(parts[3]);
                interaction.getUser().openPrivateChannel().thenAccept(channel -> {
                    List<String> msgs = store.getMessageVersions(interaction.getServer().get().getId(),
                                    channelId, msgId)
                            .join();
                    Server server = interaction.getServer().get();
                    new MessageBuilder().addEmbed(createMessage(msgId, server.getName(), server.getId(), msgs))
                            .addActionRow(msgs.size() == 1 ? BACK_DISABLED() : BACK(),
                                    isDeleted(msgs) ? MESSAGE_LINK_DISABLED() : MESSAGE_LINK("https://discord.com/channels/" + event
                                            .getButtonInteraction()
                                            .getServer()
                                            .get().getId() + "/" + channelId + "/" + msgId),
                                    isDeleted(msgs) ? DETAILS_DELETE_DISABLED() : DETAILS_DELETE(),
                                    NEXT_DISABLED())
                            .send(channel);

                });
                break;
            }
            case "back": {
                event.getButtonInteraction().createImmediateResponder().respond();
                MessageUpdater updater = new MessageUpdater(interaction.getMessage());
                DetailsData data = DetailsData.fromMessage(interaction.getMessage());

                List<String> msgs = store.getMessageVersions(data.serverId,
                                data.channelId, data.msgId)
                        .join();
                updater.removeAllEmbeds();
                updater.removeAllComponents();
                updater.addEmbed(createMessage(data.msgId, data.serverName,
                        data.serverId, msgs, data.version - 1));
                updater.addActionRow(data.version == 1 ? BACK_DISABLED() : BACK(),
                        isDeleted(msgs) ? MESSAGE_LINK_DISABLED() : MESSAGE_LINK("https://discord.com/channels/"
                                + data.serverId + "/" + data.channelId + "/" + data.msgId),
                        isDeleted(msgs) ? DETAILS_DELETE_DISABLED() : DETAILS_DELETE(), NEXT());
                updater.replaceMessage().exceptionally(ExceptionLogger.get());
                break;
            }
            case "next": {
                event.getButtonInteraction().createImmediateResponder().respond();
                MessageUpdater updater = new MessageUpdater(interaction.getMessage());
                DetailsData data = DetailsData.fromMessage(interaction.getMessage());
                List<String> msgs = store.getMessageVersions(data.serverId,
                                data.channelId, data.msgId)
                        .join();
                updater.removeAllEmbeds();
                updater.removeAllComponents();
                updater.addEmbed(createMessage(data.msgId, data.serverName,
                        data.serverId, msgs, data.version + 1));
                updater.addActionRow(BACK(),
                        isDeleted(msgs) ? MESSAGE_LINK_DISABLED() : MESSAGE_LINK("https://discord.com/channels/"
                                + data.serverId + "/" + data.channelId + "/" + data.msgId),
                        isDeleted(msgs) ? DETAILS_DELETE_DISABLED() : DETAILS_DELETE(),
                        data.version + 2 == msgs.size() ? NEXT_DISABLED() : NEXT());
                updater.replaceMessage().exceptionally(ExceptionLogger.get());
                break;
            }
            case "delete": {
                event.getButtonInteraction().createImmediateResponder().respond();
                MessageUpdater updater = new MessageUpdater(interaction.getMessage());
                DetailsData data = DetailsData.fromMessage(interaction.getMessage());
                List<String> msgs = store.getMessageVersions(data.serverId, data.channelId, data.msgId).join();
                updater.addEmbed(createMessage(data.msgId, data.serverName, data.serverId, msgs, data.version)
                        .addField("Bestätigung", "Möchtest du diese Nachricht wirklich löschen?"));
                updater.addActionRow(DETAILS_YES(), DETAILS_NO());
                updater.replaceMessage();
                break;
            }

            case "yes": {
                handleConfirmationResponse(event.getButtonInteraction(), true);
                break;
            }
            case "no": {
                event.getButtonInteraction().createImmediateResponder().respond();
                handleConfirmationResponse(event.getButtonInteraction(), false);
                break;
            }
        }
    }

    private EmbedBuilder createMessage(long msgId, String guildName, long serverId, List<String> msgs, int currentVersion) {
        String msg = msgs.get(currentVersion);
        EmbedBuilder builder = EmbedTemplate.info()
                .setTitle("Nachrichten-Details")
                .addField("Server", guildName)
                .addInlineField("Author", "<@" + getAuthorId(msg) + "> (" + getAuthorId(msg) + ")")
                .addInlineField("Message-ID", msgId + "")
                .addInlineField("Channel-ID", getChannelId(msg) + "")
                .addInlineField("Server-ID", serverId + "")
                .addInlineField(currentVersion == 0 ? "Erstellt" : (getMessage(msg).equals(" ")
                        ? "Gelöscht" : "Geupdatet"), "<t:" + getTimestamp(msg) + ">");
        if (!getMessage(msg).equals(" "))
            builder.addField("Inhalt", getMessage(msg));
        builder.addField("Stand", "<t:"
                        + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + ":R>")
                .setFooter("Version " + (currentVersion + 1) + "/" + msgs.size());
        return builder;
    }

    private EmbedBuilder createMessage(long msgId, String guildName, long serverId, List<String> msgs) {
        return createMessage(msgId, guildName, serverId, msgs, msgs.size() - 1);
    }

    private long getAuthorId(String msg) {
        return Long.parseLong(msg.substring(0, 22));
    }

    private long getChannelId(String msg) {
        return Long.parseLong(msg.substring(22, 44));
    }

    private long getTimestamp(String msg) {
        return TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(msg.substring(44, 66)));
    }

    private String getMessage(String msg) {
        return msg.substring(66);
    }

    private boolean isDeleted(List<String> msgs) {
        return getMessage(msgs.get(msgs.size() - 1)).equals(" ");
    }

    private void handleConfirmationResponse(ButtonInteraction interaction, boolean confirmed) {
        DetailsData data = DetailsData.fromMessage(interaction.getMessage());
        if (confirmed) {
            Optional<TextChannel> optChannel = interaction.getApi().getTextChannelById(data.channelId);
            if (optChannel.isPresent()) {
                Message msg = optChannel.get().getMessageById(data.msgId).join();
                if (msg.canDelete(interaction.getUser())) msg.delete();
                else
                    interaction.createImmediateResponder()
                            .addEmbed(EmbedTemplate.error()
                                    .setDescription("Du musst die Nachricht auch per Hand " +
                                            "löschen können, um sie mit dem Bot zu löschen."))
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();

            }
        }
        MessageUpdater updater = new MessageUpdater(interaction.getMessage());
        List<String> msgs = store.getMessageVersions(data.serverId, data.channelId, data.msgId).join();
        updater.addEmbed(createMessage(data.msgId, data.serverName, data.serverId, msgs));
        updater.addActionRow(data.version == 0 ? BACK_DISABLED() : BACK(),
                isDeleted(msgs) ? MESSAGE_LINK_DISABLED() : MESSAGE_LINK("https://discord.com/channels/"
                        + data.serverId + "/" + data.channelId + "/" + data.msgId),
                isDeleted(msgs) ? DETAILS_DELETE_DISABLED() : DETAILS_DELETE(), NEXT_DISABLED());
        updater.replaceMessage();
    }
}
