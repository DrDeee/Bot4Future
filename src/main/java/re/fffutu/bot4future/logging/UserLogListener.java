package re.fffutu.bot4future.logging;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.ChannelStore.ChannelType;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.UserStore;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;

public class UserLogListener implements ServerMemberJoinListener, ServerMemberLeaveListener {
    public static UserLogListener INSTANCE;
    private final UserStore store = Database.USERS;
    private final ChannelStore channelStore = Database.CHANNELS;

    public UserLogListener() {
        INSTANCE = this;
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        channelStore.getChannel(event.getServer().getId(), ChannelStore.ChannelType.USER_LOG).ifPresent(channel -> {
            User user = event.getUser();
            store.setJoinedTimestamp(event.getServer().getId(), user.getId(), Instant.now());

            MessageBuilder builder = new MessageBuilder()
                    .addEmbed(EmbedTemplate.base()
                                           .setTitle("User gejoint")
                                           .setColor(Color.GREEN)
                                           .setThumbnail(user.getAvatar())
                                           .addField("Name", user.getDiscriminatedName(), true)
                                           .addField("Anzeige-Name", user.getDisplayName(event.getServer()), true)
                                           .addField("ID", user.getId() + "", false)
                                           .addField("Erstellt",
                                                     "<t:" + user.getCreationTimestamp().getEpochSecond() + ":R>")
                    )
                    .addActionRow(new ButtonBuilder()
                                          .setCustomId("user-details:" + user.getId())
                                          .setStyle(ButtonStyle.PRIMARY)
                                          .setLabel("Details")
                                          .build());
            builder.send(channel.asTextChannel().get());
        });
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        channelStore.getChannel(event.getServer().getId(), ChannelStore.ChannelType.USER_LOG).ifPresent(channel -> {
            User user = event.getUser();
            MessageBuilder builder = new MessageBuilder();
            EmbedBuilder embedBuilder = EmbedTemplate.base()
                                                     .setTitle("User geleavt")
                                                     .setColor(Color.RED)
                                                     .setThumbnail(user.getAvatar())
                                                     .addField("Name", user.getDiscriminatedName(), true)
                                                     .addField("Anzeige-Name", user.getDisplayName(event.getServer()),
                                                               true)
                                                     .addField("ID", user.getId() + "", false)
                                                     .addField("Erstellt",
                                                               "<t:" + user.getCreationTimestamp().getEpochSecond() +
                                                                       ":R>");
            Optional<Instant> optionalInstant = store.getJoinedTimestamp(event.getServer().getId(),
                                                                         user.getId()).join();
            store.deleteJoinedTimestamp(event.getServer().getId(), user.getId());
            if (optionalInstant.isPresent()) {
                embedBuilder.addField("Gejoint",
                                      "<t:" + optionalInstant.get().getEpochSecond() + ":R>");
            }
            builder.addEmbed(embedBuilder)
                   .addActionRow(new ButtonBuilder()
                                         .setCustomId("user-details:" + user.getId())
                                         .setStyle(ButtonStyle.PRIMARY)
                                         .setLabel("Details")
                                         .build());
            builder.send(channel.asTextChannel().get());
        });
    }

    public void logAutoJoin(Server server, User user) {
        channelStore.getChannel(server.getId(), ChannelType.USER_LOG).ifPresent(log -> {
            log.asServerTextChannel()
               .get()
               .sendMessage(
                       EmbedTemplate
                               .info()
                               .setTitle("Automatische Rollenvergabe: " +
                                                 "User gejoint")
                               .setThumbnail(user.getAvatar())
                               .setDescription(user.getDiscriminatedName() + " (" +
                                                       user.getMentionTag() + ") ist über den festgelegten " +
                                                       "Einladungslink beigetreten und hat die automatische Rolle bekommen.")
                               .addField("User", user.getMentionTag())
                               .addField("User-ID", user.getIdAsString())
                               .addField("Uhrzeit", String.format("<t:%d:R>", Instant.now().getEpochSecond())));
        });
    }

    public void logUserUnlock(Server server, User user) {
        channelStore.getChannel(server.getId(), ChannelType.USER_LOG).ifPresent(log -> {
            log.asServerTextChannel()
                    .get()
                    .sendMessage(
                            EmbedTemplate
                                    .info()
                                    .setTitle("User-Freischaltung: " + user.getDiscriminatedName())
                                    .setThumbnail(user.getAvatar())
                                    .addField("User", user.getMentionTag())
                                    .addField("User-ID", user.getIdAsString())
                                    .addField("Uhrzeit", String.format("<t:%d:R>", Instant.now().getEpochSecond())));
        });
    }
}
