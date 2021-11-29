package re.fffutu.bot4future.db;

import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import re.fffutu.bot4future.DiscordBot;
import redis.clients.jedis.Jedis;

import java.nio.file.DirectoryStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ChannelStore {
    public static Optional<ServerChannel> getChannel(long guildId, ChannelType type) {
        Jedis jedis = Database.create();
        String id = jedis.get("channel:" + guildId + ":" + type);
        Database.close(jedis);
        if (id == null) return Optional.empty();
        return DiscordBot.INSTANCE.api.getServerChannelById(id);
    }

    public static void setChannel(long guildId, long channelId, ChannelType type) {

        Jedis jedis = Database.create();
        jedis.set("channel:" + guildId + ":" + type, channelId + "");
        Database.close(jedis);

    }

    public enum ChannelType {
        AUDIT("Audit-Channel"),
        PM("PM-Channel"),

        MESSAGE_LOG("Messagelog-Channel"),
        USER_LOG("Userlog-Channel"),
        SERVER_LOG("Serverlog-Channel"),
        STORE("Store-Channel");

        private final String name;
        private boolean isText;

        ChannelType(String name, boolean isText) {
            this.name = name;
            this.isText = isText;
        }

        ChannelType(String name) {
            this.name = name;
            this.isText = true;
        }

        public boolean isTextChannel() {
            return isText;
        }

        public String getName(){
            return name;
        }
    }
}
