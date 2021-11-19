package re.fffutu.bot4future.logging;

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
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.db.ChannelType;
import re.fffutu.bot4future.db.Database;

import java.awt.*;
import java.time.Instant;

import static re.fffutu.bot4future.logging.EventAuditLogButtonTemplates.*;

public class EventLogListener implements UserRoleAddListener,
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

        //TODO IGNORED CHANNELS
        store.getMessage(guildId, channelId, event.getMessageId()).thenAccept(decryptText -> {
            if (decryptText == null) decryptText = "`unknown message`";
            String oldText = decryptText;

            String newText = event.getMessage().get().getContent();

            MessageAuthor author = event.getMessageAuthor().get();
            Database.getChannel(guildId, ChannelType.EVENT_AUDIT).thenAccept(auditOpt -> {
                auditOpt.ifPresent(id -> {
                    DiscordBot.INSTANCE.api.getTextChannelById(id).ifPresent(channel -> {
                        MessageBuilder builder = new MessageBuilder();
                        builder.addActionRow(
                                MESSAGE_LINK(event.getMessageLink().get().toString()),
                                DETAILS(channelId, event.getMessageId()),
                                DELETE(channelId, event.getMessageId())
                        );
                        builder.addEmbed(new EmbedBuilder()
                                .setTimestamp(Instant.now())
                                .setColor(Color.BLUE)
                                .setTitle("Nachricht bearbeitet")
                                .setFooter(author.getDisplayName() + " (" + author.getId() + ")",
                                        author.getAvatar())
                                .addField("Channel", "<#" + event.getChannel().getId() + ">", true)
                                .addField("User", "<@" + author.getId() + ">", true)

                                .addField("Alte Nachricht", oldText)
                                .addField("Bearbeitete Nachricht", newText)
                                .addField("Message ID: ", event.getMessageId() + "", false));
                        builder.send(channel);
                    });
                });
            });

            //update encrypted message in database
            long messageId = event.getMessageId();
            long userId = author.getId();

            store.updateMessage(newText, guildId, channelId, messageId, userId);
        });
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        long guildId = event.getServer().get().getId();
        long channelId = event.getChannel().getId();


        // TODO IGNORED CHANNELS
        store.getMessage(guildId,
                channelId, event.getMessageId()).thenAccept(msgContentRaw -> {
            if (msgContentRaw == null) msgContentRaw = "`unknown message`";
            String msgContent = msgContentRaw;
            MessageAuthor author = event.getMessageAuthor().get();
            store.saveMessage(" ", guildId, channelId, event.getMessageId(), author.getId());

            Database.getChannel(guildId, ChannelType.EVENT_AUDIT).thenAccept(auditOpt -> {
                auditOpt.ifPresent(id -> {
                    DiscordBot.INSTANCE.api.getTextChannelById(id).ifPresent(channel -> {
                        MessageBuilder builder = new MessageBuilder();
                        builder.addActionRow(DETAILS(channelId, event.getMessageId()));
                        builder.addEmbed(new EmbedBuilder()
                                .setTitle("Nachricht gelöscht")
                                .setTimestamp(Instant.now())
                                .setColor(Color.ORANGE)
                                .setFooter(author.getDisplayName()
                                        + " (" + author.getId() + ")", author.getAvatar())
                                .addInlineField("Channel", "<#" + event.getChannel().getId() + ">")
                                .addInlineField("User", "<@" + author.getId() + ">")
                                .addField("Gelöschte Nachricht", msgContent)
                        );
                        builder.send(channel);
                    });
                });
            });
        });
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (!event.getServer().isPresent() || event.getMessageAuthor().isBotUser()) return;

        store.saveMessage(event.getMessageContent(),
                event.getServer().get().getId(),
                event.getChannel().getId(),
                event.getMessageId(),
                event.getMessageAuthor().getId());

    }
}
