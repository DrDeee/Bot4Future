package re.fffutu.bot4future.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Database {
    public static JedisPool POOL;
    private static Logger logger = LoggerFactory.getLogger("database");
    public static void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(100);
        config.setMaxIdle(150);
        config.setMaxWaitMillis(2000);
        POOL = new JedisPool(config,
                DiscordBot.INSTANCE.config.getString("redis.hostname"),
                DiscordBot.INSTANCE.config.getInt("redis.port"));

        logger.info("Jedis pool created.");
    }

    public static CompletableFuture<Optional<Long>> getChannel(long guildId, ChannelType type){
        return CompletableFuture.supplyAsync(() -> {
            Jedis jedis = POOL.getResource();
            String id = jedis.get("channel:" + guildId + ":" + type);
            POOL.returnResource(jedis);
            if(id == null) return Optional.empty();
            return Optional.of(Long.parseLong(id));
        });
    }

    public static CompletableFuture setChannel(long guildId, long channelId, ChannelType type){
        return CompletableFuture.runAsync(() -> {
            Jedis jedis = POOL.getResource();
            jedis.set("channel:" + guildId + ":" + type, channelId + "");
            POOL.returnResource(jedis);
        });
    }
}
