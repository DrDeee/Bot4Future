package re.fffutu.bot4future.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Database {
    private static Logger logger = LoggerFactory.getLogger("database");

    public static void init() {
       DiscordBot.POOL.scheduleAtFixedRate(() -> {
            Jedis jedis = create();
            jedis.bgsave();
            close(jedis);
            logger.debug("Database saved.");
        }, 10, 30, TimeUnit.SECONDS);
    }

    public static Jedis create() {
        return new Jedis(new HostAndPort(DiscordBot.INSTANCE.config.getString("redis.hostname"),
                DiscordBot.INSTANCE.config.getInt("redis.port")));
    }

    public static void close(Jedis jedis) {
        jedis.close();
    }
}
