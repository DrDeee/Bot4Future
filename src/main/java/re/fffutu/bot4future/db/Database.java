package re.fffutu.bot4future.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class Database {
    public static final AuditStore AUDIT = new AuditStore();
    public static final ChannelStore CHANNELS = new ChannelStore();
    public static final MessageStore MESSAGES = new MessageStore();
    public static final RoleStore ROLES = new RoleStore();
    public static final ServerStore SERVERS = new ServerStore();
    public static final UserStore USERS = new UserStore();

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
