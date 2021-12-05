package re.fffutu.bot4future.db;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuditStore {
    private final Gson GSON = new Gson();

    public static class AuditEntry {
        public UUID id = UUID.randomUUID();
        public long creatorId = 0;
        public String reason;
        public long userId = 0;
        public long createdAt = 0;
        public long expiresAt = 0;


        public AuditType type;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AuditEntry && (((AuditEntry) obj).id).equals(id)) return true;
            return false;
        }
    }

    public enum AuditType {
        BAN,
        MUTE,
        WARN
    }

    public List<AuditEntry> getAuditEntries(long guildId, long userId) {
        Jedis jedis = Database.create();
        List<AuditEntry> entries = jedis.lrange("audit:" + guildId + ":" + userId, 0, -1)
                .stream()
                .map(data -> GSON.fromJson(data, AuditEntry.class))
                .collect(Collectors.toList());
        Database.close(jedis);
        return entries;
    }

    public void addAuditEntry(long guildId, long userId, AuditEntry entry) {
        Jedis jedis = Database.create();
        jedis.rpush("audit:" + guildId + ":" + userId, GSON.toJson(entry));
        Database.close(jedis);
    }

    public void updateAuditEntry(long guildId, long userId, UUID id, AuditEntry entry) {
        List<AuditEntry> entries = getAuditEntries(guildId, userId);
        Jedis jedis = Database.create();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).equals(entry)) {
                jedis.lset("audit:" + guildId + ":" + userId, i, GSON.toJson(entry));
            }
        }
        Database.close(jedis);
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

        public List<AuditEntry> get() {
            return entries;
        }
    }
}
