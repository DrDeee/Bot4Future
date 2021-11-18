package re.fffutu.bot4future.logging;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.logging.ExceptionLogger;
import re.fffutu.bot4future.EmbedTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MessageDetails implements ButtonClickListener {
    MessageStore store = new MessageStore();

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

    private void notFound(ButtonClickEvent event) {
        event.getButtonInteraction().createImmediateResponder()
                .addEmbed(EmbedTemplate.error()
                        .setDescription("Die Nachricht konnte nicht gefunden werden!"))
                .setFlags(MessageFlag.EPHEMERAL)
                .respond();
    }

    private void askForConfirm(ButtonClickEvent event) {
        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        long channelId = Long.parseLong(parts[1]);
        long msgId = Long.parseLong(parts[2]);


        event.getButtonInteraction().createImmediateResponder()
                .addEmbed(EmbedTemplate.info().setDescription("Bist du sicher," +
                        " dass du diese Nachricht löschen willst?"))
                .addComponents(new ActionRowBuilder()
                        .addComponents(new ButtonBuilder()
                                        .setLabel("Ja")
                                        .setStyle(ButtonStyle.SUCCESS)
                                        .setCustomId("confirm-delete:" + channelId + ":" + msgId)
                                        .build(),
                                new ButtonBuilder()
                                        .setLabel("Nein")
                                        .setStyle(ButtonStyle.DANGER)
                                        .setCustomId("abort-delete:" + channelId + ":" + msgId)
                                        .build()
                        )
                        .build())
                .setFlags(MessageFlag.EPHEMERAL)
                .respond();
    }

    private void delete(ButtonClickEvent event) {
        String[] parts = event.getButtonInteraction().getCustomId().split(":");
        long channelId = Long.parseLong(parts[1]);
        long msgId = Long.parseLong(parts[2]);

        Optional<ServerTextChannel> optChannel = event.getInteraction().getServer()
                .get()
                .getTextChannelById(channelId);
        if (optChannel.isPresent()) {
            optChannel.get().getMessageById(msgId).thenAccept(msg -> {
                msg.delete().thenAccept((v) -> event.getButtonInteraction().createImmediateResponder()
                        .addEmbed(EmbedTemplate.success().setDescription("Die Nachricht wurde gelöscht."))
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond()).exceptionally(ExceptionLogger.get());
            });
        } else notFound(event);
        event.getButtonInteraction().getMessage().ifPresent(message -> message.delete());
    }

    private void abort(ButtonClickEvent event) {
        event.getButtonInteraction().createImmediateResponder()
                .addEmbed(EmbedTemplate.success()
                        .setDescription("Das Löschen wurde abgebrochen."))
                .setFlags(MessageFlag.EPHEMERAL)
                .respond();
    }
}

