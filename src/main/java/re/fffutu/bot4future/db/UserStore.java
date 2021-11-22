package re.fffutu.bot4future.db;

import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserStore {
    public static CompletableFuture<Optional<Instant>> getJoinedTimestamp(long serverId, long userId) {
        return CompletableFuture.supplyAsync(() -> {
            Jedis jedis = Database.create();
            String ts = jedis.get("user:joinTs:" + userId + ":" + serverId);
            if (ts.equals("nil")) return Optional.empty();
            Database.close(jedis);
            return Optional.of(Instant.ofEpochSecond(Long.parseLong(ts)));
        });
    }

    public static CompletableFuture setJoinedTimestamp(long serverId, long userId, Instant ts) {
        return CompletableFuture.runAsync(() -> {
            Jedis jedis = Database.create();
            jedis.set("user:joinTs:" + userId + ":" + serverId, ts.getEpochSecond() + "");
            Database.close(jedis);
        });
    }

    public static CompletableFuture deleteJoinedTimestamp(long serverId, long userId) {
        return CompletableFuture.runAsync(() -> {
            Jedis jedis = Database.create();
            jedis.del("user:joinTs:" + userId + ":" + serverId);
            Database.close(jedis);
        });
    }
}
