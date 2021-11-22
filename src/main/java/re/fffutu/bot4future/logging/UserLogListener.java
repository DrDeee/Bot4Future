package re.fffutu.bot4future.logging;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.ChannelType;
import re.fffutu.bot4future.db.UserStore;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;

public class UserLogListener implements ServerMemberJoinListener, ServerMemberLeaveListener {
    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        ChannelStore.getChannel(event.getServer().getId(), ChannelType.USER_LOG).thenAccept(optId -> {
            optId.ifPresent(id -> {
                User user = event.getUser();
                UserStore.setJoinedTimestamp(event.getServer().getId(), user.getId(), Instant.now());
                DiscordBot.INSTANCE.api.getServerTextChannelById(id).ifPresent(channel -> {
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
                    builder.send(channel);
                });
            });
        });
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        ChannelStore.getChannel(event.getServer().getId(), ChannelType.USER_LOG).thenAccept(optId -> {
            optId.ifPresent(id -> {
                User user = event.getUser();
                DiscordBot.INSTANCE.api.getServerTextChannelById(id).ifPresent(channel -> {
                    MessageBuilder builder = new MessageBuilder();
                    EmbedBuilder embedBuilder = EmbedTemplate.base()
                            .setTitle("User geleavt")
                            .setColor(Color.RED)
                            .setThumbnail(user.getAvatar())
                            .addField("Name", user.getDiscriminatedName(), true)
                            .addField("Anzeige-Name", user.getDisplayName(event.getServer()), true)
                            .addField("ID", user.getId() + "", false)
                            .addField("Erstellt",
                                    "<t:" + user.getCreationTimestamp().getEpochSecond() + ":R>");
                    Optional<Instant> optionalInstant = UserStore.getJoinedTimestamp(event.getServer().getId(),
                            user.getId()).join();
                    UserStore.deleteJoinedTimestamp(event.getServer().getId(), user.getId());
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
                    builder.send(channel);
                });
            });
        });
    }
}
