package re.fffutu.bot4future.logging;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.AuditStore.AuditEntry;
import re.fffutu.bot4future.db.ChannelStore.ChannelType;
import re.fffutu.bot4future.db.Database;

import java.time.Instant;

public class AuditLogger {

    public static void logAudit(Server server, AuditEntry entry) {
        Database.CHANNELS.getChannel(server.getId(), ChannelType.AUDIT).ifPresent(log -> {
            User target = server.getApi().getUserById(entry.userId).join();
            User creator = server.getApi().getUserById(entry.creatorId).join();

            log.asTextChannel().get()
               .sendMessage(EmbedTemplate.info()
                                         .setTitle(entry.type.asString() + ": " + target.getDiscriminatedName())
                                         .setThumbnail(target.getAvatar())
                                         .setDescription(target.getDiscriminatedName() + " (" + target.getMentionTag() +
                                                                 ") wurde von " +
                                                                 creator.getDiscriminatedName() + " (" +
                                                                 creator.getMentionTag() + ") " +
                                                                 entry.type.getActionPart() + ".")
                                         .addField("Grund", entry.reason)
                                         .addField("Ablauf", entry.expiresAt == 0 ? "Nie (Permanent)" :
                                                 "<t:" + Instant.ofEpochMilli(entry.expiresAt).getEpochSecond() +
                                                         ":R>"));
        });
    }

    public static void logAuditEnd(Server server, AuditEntry entry) {
        Database.CHANNELS.getChannel(server.getId(), ChannelType.AUDIT).ifPresent(log -> {
            User target = server.getApi().getUserById(entry.userId).join();

            log.asTextChannel().get()
               .sendMessage(EmbedTemplate.info()
                                         .setTitle(entry.type.asString() + " beendet: " + target.getDiscriminatedName())
                                         .setThumbnail(target.getAvatar())
                                         .setDescription(target.getDiscriminatedName() + " (" + target.getMentionTag() +
                                                                 ") wurde automatisch " +
                                                                 entry.type.getActionEndPart() + ".")
                                         .addField("Grund", entry.reason)
                                         .addField("Start",
                                                   "<t:" + Instant.ofEpochMilli(entry.createdAt).getEpochSecond() +
                                                           ":R>")
                                         .addField("Ende",
                                                   "<t:" + Instant.ofEpochMilli(entry.expiresAt).getEpochSecond() +
                                                           ":R>"));
        });
    }
}
