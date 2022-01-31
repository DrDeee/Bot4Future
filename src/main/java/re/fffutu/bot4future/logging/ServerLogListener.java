package re.fffutu.bot4future.logging;

import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.thread.ThreadCreateEvent;
import org.javacord.api.event.channel.thread.ThreadDeleteEvent;
import org.javacord.api.event.channel.thread.ThreadUpdateEvent;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelCreateListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelDeleteListener;
import org.javacord.api.listener.channel.server.thread.ServerThreadChannelUpdateListener;
import org.javacord.api.util.logging.ExceptionLogger;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.ServerStore;

public class ServerLogListener implements ServerThreadChannelCreateListener, ServerThreadChannelDeleteListener,
        ServerThreadChannelUpdateListener {
    private final ServerStore serverStore = Database.SERVERS;
    private final ChannelStore channelStore = Database.CHANNELS;

    @Override
    public void onThreadCreate(ThreadCreateEvent event) {
        if (!serverStore.isInSet(event.getServer().getId(), ServerStore.LOGGED_THREADS,
                                 event.getChannel().getIdAsString())) {
            serverStore.addToSet(event.getServer().getId(), ServerStore.LOGGED_THREADS,
                                 event.getChannel().getIdAsString());
            channelStore.getChannel(event.getServer().getId(), ChannelStore.ChannelType.SERVER_LOG)
                        .ifPresent(channel -> {
                            AuditLog log = event.getServer().getAuditLog(50).join();
                            log.getEntries()
                               .stream()
                               .filter(entry -> entry.getType() == AuditLogActionType.THREAD_CREATE
                                       && entry.getTarget().get().getId() == event.getChannel().getId())
                               .findAny()
                               .ifPresent(entry -> {
                                   ServerThreadChannel thread = event.getChannel().asServerThreadChannel().get();
                                   thread.joinThread().exceptionally(ExceptionLogger.get()).join();
                                   User creator = event.getApi().getUserById(thread.getOwnerId()).join();
                                   channel.asTextChannel().get().sendMessage(EmbedTemplate.success()
                                                                                          .setTitle("Thread erstellt")
                                                                                          .addField("Thread-Name",
                                                                                                    thread.getMentionTag(),
                                                                                                    true)
                                                                                          .addField("Thread-ID",
                                                                                                    thread.getId() + "",
                                                                                                    true)
                                                                                          .addField("Ersteller",
                                                                                                    creator.getMentionTag() +
                                                                                                            " (" +
                                                                                                            creator.getId() +
                                                                                                            ")", false)
                                                                                          .addField("Channel",
                                                                                                    thread.getParent()
                                                                                                          .getMentionTag(),
                                                                                                    true)
                                                                                          .addField("Channel-ID",
                                                                                                    thread.getParent()
                                                                                                          .getId() + "",
                                                                                                    true)
                                                                                          .addField("Erstellt", "<t:" +
                                                                                                  thread.getCreationTimestamp()
                                                                                                        .getEpochSecond() +
                                                                                                  ":R>")
                                                                                          .addField("Privater Thread",
                                                                                                    thread.isPrivate() ?
                                                                                                            "Ja" :
                                                                                                            "Nein")
                                                                                          .addField("Archiviert in",
                                                                                                    thread.getAutoArchiveDuration() +
                                                                                                            " Minuten")

                                   );
                               });
                        });
        }
    }

    @Override
    public void onThreadDelete(ThreadDeleteEvent event) {
        channelStore.getChannel(event.getServer().getId(), ChannelStore.ChannelType.SERVER_LOG).ifPresent(channel -> {
            AuditLog log = event.getServer().getAuditLog(50).join();
            log.getEntries().stream().filter(entry -> entry.getType() == AuditLogActionType.THREAD_DELETE
                    && entry.getTarget().get().getId() == event.getChannel().getId()).findAny().ifPresent(entry -> {
                ServerThreadChannel thread = event.getChannel().asServerThreadChannel().get();
                User creator = event.getApi().getUserById(thread.getOwnerId()).join();
                User deleter = entry.getUser().join();
                channel.asTextChannel().get().sendMessage(EmbedTemplate.error()
                                                                       .setTitle("Thread gelöscht")
                                                                       .addField("Thread-Name", thread.getName(), true)
                                                                       .addField("Thread-ID", thread.getId() + "", true)
                                                                       .addField("Ersteller",
                                                                                 creator.getMentionTag() + " (" +
                                                                                         creator.getId() + ")", false)
                                                                       .addField("Channel",
                                                                                 thread.getParent().getMentionTag(),
                                                                                 true)
                                                                       .addField("Channel-ID",
                                                                                 thread.getParent().getId() + "", true)
                                                                       .addField("Thread-Status", "Archiviert: " +
                                                                               (thread.isArchived() ? "Ja" : "Nein")
                                                                               + "\nGesperrt: " +
                                                                               (thread.isLocked() ? "Ja" : "Nein")
                                                                               + "\nPrivat: " +
                                                                               (thread.isPrivate() ? "Ja" : "Nein"))
                                                                       .addField("Mitglieder",
                                                                                 thread.getMemberCount() + "")
                                                                       .addField("Nachrichten",
                                                                                 thread.getMessageCount() + "")
                                                                       .addField("Löschender User",
                                                                                 deleter.getMentionTag() + " (" +
                                                                                         deleter.getId() + ")")
                                                                       .addField("Erstellt", "<t:" +
                                                                               thread.getCreationTimestamp()
                                                                                     .getEpochSecond() + ":R>")
                                                                       .addField("Gelöscht", "<t:" +
                                                                               entry.getCreationTimestamp()
                                                                                    .getEpochSecond() + ":R>")

                );
            });
        });
    }

    @Override
    public void onThreadUpdate(ThreadUpdateEvent event) {
        channelStore.getChannel(event.getServer().getId(), ChannelStore.ChannelType.SERVER_LOG).ifPresent(channel -> {
            ServerThreadChannel thread = event.getChannel().asServerThreadChannel().get();
            User creator = event.getApi().getUserById(thread.getOwnerId()).join();
            channel.asTextChannel().get().sendMessage(EmbedTemplate.info()
                                                                   .setTitle("Thread geupdated")
                                                                   .addField("Thread-Name", thread.getMentionTag(),
                                                                             true)
                                                                   .addField("Thread-ID", thread.getId() + "", true)
                                                                   .addField("Ersteller",
                                                                             creator.getMentionTag() + " (" +
                                                                                     creator.getId() + ")", false)
                                                                   .addField("Channel",
                                                                             thread.getParent().getMentionTag(), true)
                                                                   .addField("Channel-ID",
                                                                             thread.getParent().getId() + "", true)
                                                                   .addField("Erstellt", "<t:" +
                                                                           thread.getCreationTimestamp()
                                                                                 .getEpochSecond() + ":R>")
                                                                   .addField("Thread-Status", "Archiviert: " +
                                                                           (thread.isArchived() ? "Ja" : "Nein")
                                                                           + "\nGesperrt: " +
                                                                           (thread.isLocked() ? "Ja" : "Nein")
                                                                           + "\nPrivat: " +
                                                                           (thread.isPrivate() ? "Ja" : "Nein"))
                                                                   .addField("Archiviert in",
                                                                             thread.getAutoArchiveDuration() +
                                                                                     " Minuten")

            );
        });
    }
}
