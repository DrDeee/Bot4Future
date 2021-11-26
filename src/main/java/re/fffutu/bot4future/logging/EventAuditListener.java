package re.fffutu.bot4future.logging;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.ChannelStore.ChannelType;
import re.fffutu.bot4future.db.MessageStore;

import java.awt.*;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static re.fffutu.bot4future.logging.EventAuditLogButtonTemplates.*;

public class EventAuditListener implements UserRoleAddListener,
        UserRoleRemoveListener,
        MessageEditListener,
        MessageDeleteListener,
        MessageCreateListener {

    private MessageStore store = new MessageStore();

    @Override
    public void onUserRoleAdd(UserRoleAddEvent userRoleAddEvent) {

    }

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent userRoleRemoveEvent) {

    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (!event.getServer().isPresent()
                || !event.getMessageAuthor().isPresent()
                || event.getMessageAuthor().get().isBotUser()) return;

        long guildId = event.getServer().get().getId();
        long channelId = event.getChannel().getId();

        Message msg = event.requestMessage().join();
        String newText = msg.getContent();

        //TODO IGNORED CHANNELS
        store.getMessage(guildId, channelId, event.getMessageId()).thenAccept(msgData -> {
            String oldText;
            if (msgData == null) return;
            else oldText = msgData.content;

            MessageAuthor author = event.getMessageAuthor().get();
            ChannelStore.getChannel(guildId, ChannelType.EVENT_AUDIT).ifPresent(id ->
                    DiscordBot.INSTANCE.api.getTextChannelById(id).ifPresent(channel -> {
                        MessageBuilder builder = new MessageBuilder();
                        builder.addActionRow(
                                MESSAGE_LINK(event.getMessageLink().get().toString()),
                                DETAILS(channelId, event.getMessageId()),
                                DELETE(channelId, event.getMessageId())
                        );
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(Color.BLUE)
                                .setTitle("Nachricht bearbeitet")
                                .setFooter(author.getDisplayName() + " (" + author.getId() + ")",
                                        author.getAvatar())
                                .addField("Channel", "<#" + event.getChannel().getId() + ">", true)
                                .addField("User", "<@" + author.getId() + ">", true)
                                .addField("Alte Nachricht", oldText.equals("") ? "*Kein Inhalt*" : oldText)
                                .addField("Bearbeitete Nachricht", newText.equals("") ? "*Kein Inhalt*" : newText);
                        List<String> files = getFilesFromMessage(msg);
                        if (files.size() != 0)
                            embedBuilder.addField("Jetzige Anhänge", formatAttachments(files));

                        embedBuilder
                                .addField("Message ID: ", event.getMessageId() + "", false);
                        if (oldText.equals("") || newText.equals("")) embedBuilder.addField("Hinweis",
                                "Die alte Nachricht hatte " + (oldText.equals("")
                                        ? "nur Anhänge" : (msgData.files.size() == 0 ? "nur Text" : "Text und Anhänge"))
                                        + " und hat nun " + (newText.equals("")
                                        ? "nur Anhänge" : (files.size() == 0 ? "nur Text" : "Text und Anhänge")) + ".");
                        builder.addEmbed(embedBuilder);
                        builder.send(channel);
                    }));
        });
        //update encrypted message in database
        long messageId = event.getMessageId();
        long userId = msg.getAuthor().getId();
        store.updateMessage(newText, guildId, channelId, messageId, userId,
                storeFilesFromMessage(event.getMessage().get()));
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (!event.getServer().isPresent()) return;
        long guildId = event.getServer().get().getId();
        long channelId = event.getChannel().getId();


        // TODO IGNORED CHANNELS
        store.getMessage(guildId,
                channelId, event.getMessageId()).thenAccept(msgData -> {
            String msgContent;
            if (msgData == null) return;
            else msgContent = msgData.content;
            MessageAuthor author = event.getMessageAuthor().get();
            store.saveMessage(null, guildId, channelId, event.getMessageId(), author.getId(), new ArrayList<>());

            ChannelStore.getChannel(guildId, ChannelType.EVENT_AUDIT).ifPresent(id ->
                    DiscordBot.INSTANCE.api.getTextChannelById(id).ifPresent(channel -> {
                        MessageBuilder builder = new MessageBuilder();
                        builder.addActionRow(DETAILS(channelId, event.getMessageId()));
                        EmbedBuilder eBuilder = new EmbedBuilder()
                                .setTitle("Nachricht gelöscht")
                                .setTimestamp(Instant.now())
                                .setColor(Color.ORANGE)
                                .setFooter(author.getDisplayName()
                                        + " (" + author.getId() + ")", author.getAvatar())
                                .addInlineField("Channel", "<#" + event.getChannel().getId() + ">")
                                .addInlineField("User", "<@" + author.getId() + ">")
                                .addField("Gelöschte Nachricht",
                                        msgContent.equals("") ? "*Kein Inhalt*" : msgContent);
                        if (msgData.files.size() != 0)
                            eBuilder.addField("Anhänge", formatAttachments(msgData.files));
                        if (msgContent.equals("")) eBuilder.addField("Hinweis", "Diese Nachricht hatte nur Text.");
                        builder.addEmbed(eBuilder);
                        builder.send(channel);
                    }));
        });
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (!event.getServer().isPresent() || !event.getMessageAuthor().isRegularUser()) return;
        store.saveMessage(event.getMessageContent(),
                event.getServer().get().getId(),
                event.getChannel().getId(),
                event.getMessageId(),
                event.getMessageAuthor().getId(),
                storeFilesFromMessage(event.getMessage()));

        if (event.getMessageAttachments().size() != 0) {
            long serverId = event.getServer().get().getId();
            long channelId = event.getChannel().getId();
            MessageAuthor author = event.getMessageAuthor();
            ChannelStore.getChannel(serverId, ChannelType.EVENT_AUDIT).ifPresent(id -> {
                DiscordBot.INSTANCE.api.getTextChannelById(id).ifPresent(channel -> {
                    String content = event.getMessageContent();
                    try {
                        MessageBuilder builder = new MessageBuilder();
                        builder.addActionRow(DETAILS(channelId, event.getMessageId()));
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(Color.GREEN)
                                .setTitle("Nachricht mit Anhang gesendet")
                                .setFooter(author.getDisplayName() + " (" + author.getId() + ")",
                                        author.getAvatar())
                                .addField("Channel", "<#" + event.getChannel().getId() + ">", true)
                                .addField("User", "<@" + author.getId() + ">", true)

                                .addField("Inhalt", content.equals("") ? "*Kein Inhalt*" : content)
                                .addField("Anhänge",
                                        formatAttachments(getFilesFromMessage(event.getMessage())))
                                .addField("Message ID: ", event.getMessageId() + "", false);
                        if (content.equals(""))
                            embedBuilder.addField("Hinweis", "Diese Nachricht hat keinen Text.");
                        builder.addEmbed(embedBuilder);
                        builder.send(channel).exceptionally(ExceptionLogger.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

    private List<String> getFilesFromMessage(Message msg) {
        return msg.getAttachments().stream().map(MessageAttachment::getUrl)
                .map(URL::toString).collect(Collectors.toList());
    }

    private List<String> storeFilesFromMessage(Message msg) {
        if (msg.getAttachments().size() == 0) return new ArrayList<>();
        Optional<Long> storeOpt = ChannelStore.getChannel(msg.getServer().get().getId(), ChannelType.STORE);
        if (storeOpt.isPresent()) {
            Optional<TextChannel> channelOpt = DiscordBot.INSTANCE.api.getTextChannelById(storeOpt.get());
            if (channelOpt.isPresent()) {
                MessageBuilder builder = new MessageBuilder()
                        .addEmbed(EmbedTemplate.info()
                                .setTitle("Nachrichten-Anhänge")
                                .setDescription("Nachrichten-Anhänge werden in diesem Channel gespeichert," +
                                        " um dies zu Erhalten, auch wenn die ursprünglichen Nachrichten" +
                                        " gelöscht wurden."))
                        .addActionRow(EventAuditLogButtonTemplates.MESSAGE_LINK(msg.getLink().toString()));
                for (MessageAttachment attachment : msg.getAttachments()) {
                    builder.addAttachment(attachment.getUrl());
                }
                return getFilesFromMessage(builder.send(channelOpt.get()).join());
            }
        }
        return getFilesFromMessage(msg);
    }

    private String formatAttachments(List<String> files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < files.size() + 1; i++) {
            sb.append("[Anhang " + i + "](" + files.get(i - 1) + ")\n");
        }
        return sb.toString();
    }
}
