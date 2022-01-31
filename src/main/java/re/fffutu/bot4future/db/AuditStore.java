package re.fffutu.bot4future.db;

import com.google.gson.Gson;
import re.fffutu.bot4future.db.TimedTaskStore.TimedTaskType;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuditStore {
    private final Gson GSON = new Gson();

    private List<AuditEntry> getAuditEntries() {
        Jedis jedis = Database.create();
        List<AuditEntry> entries = jedis.lrange("audit", 0, -1)
                                        .stream()
                                        .map(data -> GSON.fromJson(data, AuditEntry.class))
                                        .sorted((entry1, entry2) -> entry1.expiresAt <= entry2.expiresAt ? -1 :
                                                entry1.expiresAt == entry2.expiresAt ? 0 : 1
                                        )
                                        .collect(Collectors.toList());
        Database.close(jedis);
        return entries;
    }

    public List<AuditEntry> getAuditEntries(long guildId, long userId) {
        return new AuditEntryFilter(getAuditEntries()).server(guildId).user(userId).get();
    }

    public Optional<AuditEntry> getAuditEntryById(UUID id) {
        return new AuditEntryFilter(getAuditEntries()).id(id).get().stream().findAny();
    }

    public void addAuditEntry(AuditEntry entry) {
        Jedis jedis = Database.create();
        jedis.rpush("audit", GSON.toJson(entry));
        Database.close(jedis);
    }

    public enum AuditType {
        BAN("Bann", "gebannt", "entbannt", TimedTaskType.UNBAN),
        MUTE("Mute", "gemutet", "entmutet", TimedTaskType.UNMUTE),
        WARN("Verwarnung", "verwarnt", null, null);

        private String name;
        private String actionPart;
        private String actionEndPart;
        private TimedTaskType taskType;

        AuditType(String name, String actionPart, String actionEndPart, TimedTaskType taskType) {
            this.name = name;
            this.actionPart = actionPart;
            this.actionEndPart = actionEndPart;
            this.taskType = taskType;
        }

        public String asString() {
            return name;
        }

        public String getActionPart() {
            return actionPart;
        }

        public TimedTaskType getTimedTaskType() {
            return taskType;
        }

        public String getActionEndPart() {
            return actionEndPart;
        }
    }

    public static class AuditEntry {
        public UUID id = UUID.randomUUID();
        public long creatorId = 0;
        public String reason;
        public long userId = 0;
        public long createdAt = Instant.now().toEpochMilli();
        public long expiresAt = 0;
        public long serverId = 0;
        public List<Long> roles = new ArrayList<>();


        public AuditType type;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AuditEntry && (((AuditEntry) obj).id).equals(id)) return true;
            return false;
        }
    }

    public static class AuditEntryFilter {
        private List<AuditEntry> entries;

        public AuditEntryFilter(List<AuditEntry> entries) {
            this.entries = entries;
        }

        public AuditEntryFilter actualOnly() {
            entries = entries.stream()
                             .filter(entry -> entry.expiresAt == 0 || entry.expiresAt >= Instant.now().getEpochSecond())
                             .collect(Collectors.toList());
            return this;
        }

        public AuditEntryFilter expiredOnly() {
            entries = entries.stream()
                             .filter(entry -> entry.expiresAt != 0 || entry.expiresAt < Instant.now().getEpochSecond())
                             .collect(Collectors.toList());
            return this;
        }

        public AuditEntryFilter ofType(AuditType type) {
            entries = entries.stream()
                             .filter(entry -> entry.type == type)
                             .collect(Collectors.toList());
            return this;
        }

        public AuditEntryFilter server(long serverId) {
            entries = entries.stream()
                             .filter(entry -> entry.serverId == serverId)
                             .collect(Collectors.toList());
            return this;
        }

        public AuditEntryFilter user(long userID) {
            entries = entries.stream()
                             .filter(entry -> entry.serverId == userID)
                             .collect(Collectors.toList());
            return this;
        }

        public AuditEntryFilter id(UUID id) {
            entries = entries.stream()
                             .filter(entry -> entry.id.equals(id))
                             .collect(Collectors.toList());
            return this;
        }

        public List<AuditEntry> get() {
            return entries;
        }
    }
}
