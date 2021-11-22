package re.fffutu.bot4future.db;

import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChannelStore {
    public static CompletableFuture<Optional<Long>> getChannel(long guildId, ChannelType type) {
        return CompletableFuture.supplyAsync(() -> {
            Jedis jedis = Database.create();
            String id = jedis.get("channel:" + guildId + ":" + type);
            Database.close(jedis);
            if (id == null) return Optional.empty();
            return Optional.of(Long.parseLong(id));
        });
    }

    public static CompletableFuture setChannel(long guildId, long channelId, ChannelType type) {
        return CompletableFuture.runAsync(() -> {
            Jedis jedis = Database.create();
            jedis.set("channel:" + guildId + ":" + type, channelId + "");
            Database.close(jedis);
        });
    }
}
