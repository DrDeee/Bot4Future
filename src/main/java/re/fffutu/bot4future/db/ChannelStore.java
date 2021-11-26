package re.fffutu.bot4future.db;

import org.javacord.api.entity.channel.ChannelType;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChannelStore {
    public static Optional<Long> getChannel(long guildId, ChannelType type) {
        Jedis jedis = Database.create();
        String id = jedis.get("channel:" + guildId + ":" + type);
        Database.close(jedis);
        if (id == null) return Optional.empty();
        return Optional.of(Long.parseLong(id));
    }

    public static void setChannel(long guildId, long channelId, ChannelType type) {

        Jedis jedis = Database.create();
        jedis.set("channel:" + guildId + ":" + type, channelId + "");
        Database.close(jedis);

    }

    public enum ChannelType {
        AUDIT,
        QUESTION,
        PM,

        EVENT_AUDIT,
        USER_LOG,
        STORE
    }
}
