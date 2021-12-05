package re.fffutu.bot4future.db;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import re.fffutu.bot4future.DiscordBot;
import redis.clients.jedis.Jedis;

import java.util.Optional;

public class RoleStore {
    public Optional<Role> getRole(long serverId, RoleType type) {
        Jedis jedis = Database.create();
        String rId = jedis.get("role:" + serverId + ":" + type);
        Database.close(jedis);
        if (rId == null) return Optional.empty();
        return DiscordBot.INSTANCE.api.getRoleById(rId);
    }

    public void setRole(long serverId, RoleType type, long roleId) {
        Jedis jedis = Database.create();
        jedis.set("role:" + serverId + ":" + type, roleId + "");
        Database.close(jedis);
    }

    public enum RoleType {
        ADMINISTRATOR("Administrator"),
        MODERATOR("Moderator");

        private String display;

        RoleType(String display) {
            this.display = display;
        }


        public String getName() {
            return display;
        }
        @Override
        public String toString() {
            return display.toLowerCase();
        }
    }
}
