package re.fffutu.bot4future.logging.actions;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.exception.UnknownMessageException;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static re.fffutu.bot4future.logging.EventAuditLogButtonTemplates.*;

public class MessageDeleteActionListener implements ButtonClickListener {
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        switch (event.getButtonInteraction().getCustomId().split(":")[0]) {
            case "msg-delete": {
                askForConfirm(event);
                break;
            }
            case "confirm-delete": {
                delete(event);
                break;
            }
            case "abort-delete": {
                abort(event);
                break;
            }

        }
    }

    private void askForConfirm(ButtonClickEvent event) {
        event.getInteraction().createImmediateResponder().respond();
        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        long channelId = Long.parseLong(parts[1]);
        long msgId = Long.parseLong(parts[2]);

        Message msg = event.getButtonInteraction().getMessage();
        MessageUpdater updater = new MessageUpdater(msg);
        EmbedBuilder builder = msg.getEmbeds().get(0).toBuilder();
        updater.removeEmbed(builder);

        builder.addField("Status", "Möchtest du diese Nachricht wirklich löschen?");
        updater.addEmbed(builder);

        updater.removeAllComponents();
        updater.addActionRow(YES(channelId, msgId), NO(channelId, msgId));
        updater.applyChanges();
        return;
    }

    private void delete(ButtonClickEvent event) {
        event.getInteraction().createImmediateResponder().respond();
        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        long msgId = Long.parseLong(parts[2]);
        long channelId = Long.parseLong(parts[1]);

        Optional<ServerTextChannel> optChannel = event.getApi().getServerTextChannelById(channelId);

        boolean notFound = false;
        boolean otherError = false;
        if (optChannel.isPresent()) {
            try {
                Message msg = optChannel.get().getMessageById(msgId).join();
                msg.delete().join();
            } catch (CompletionException e) {
                notFound = e.getCause() instanceof UnknownMessageException;
                otherError = !notFound;
            }
        } else notFound = true;


        Message msg = event.getButtonInteraction().getMessage();
        MessageUpdater updater = new MessageUpdater(msg);
        EmbedBuilder builder = msg.getEmbeds().get(0).toBuilder();
        updater.removeEmbed(builder);

        builder.removeFields(field -> field.getName().equals("Status"));
        builder.addField("Status", notFound ? "Die Nachricht wurde bereits gelöscht oder " +
                "konnte nicht gefunden werden." : (otherError ?
                "Beim Löschen ist ein Fehler aufgetreten." : "Die Nachricht wurde gelöscht."));
        updater.addEmbed(builder);

        updater.removeAllComponents();
        updater.addActionRow(MESSAGE_LINK_DISABLED(), DETAILS(channelId, msgId), DELETE_DISABLED());
        updater.applyChanges();
    }

    private void abort(ButtonClickEvent event) {
        event.getInteraction().createImmediateResponder().respond();
        Message msg = event.getButtonInteraction().getMessage();
        MessageUpdater updater = new MessageUpdater(msg);
        EmbedBuilder builder = msg.getEmbeds().get(0).toBuilder();
        updater.removeEmbed(builder);

        builder.removeFields(field -> field.getName().equals("Status"));
        builder.addField("Status", "Löschen abgebrochen");
        updater.addEmbed(builder);

        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        long msgId = Long.parseLong(parts[2]);
        long channelId = Long.parseLong(parts[1]);

        updater.removeAllComponents();
        updater.addActionRow(
                MESSAGE_LINK("https://discord.com/channels/" + event
                        .getButtonInteraction()
                        .getServer()
                        .get().getId() + "/" + channelId + "/" + msgId),
                DETAILS(channelId, msgId),
                DELETE(channelId, msgId));
        updater.applyChanges();
    }
}

