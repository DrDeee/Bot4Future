package re.fffutu.bot4future.db;

import org.javacord.api.entity.permission.Role;
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
        DiscordBot.INSTANCE.commandManager.updatePermissions(serverId);
    }

    public enum RoleType {
        ADMINISTRATOR("Administrator:in", "Administrator:innen-"),
        MODERATOR("Moderator:in", "Moderator:innen-"),
        MUTED("Mute-Rolle", "Mute-"),
        AUTO_ROLE("Auto-Rolle", "Auto-"),
        GIVABLE_ROLE("Gebbare Rolle", "Gebbare "),

        MEMBER("Mitglieds-Rolle", "Mitglieds-");

        private final String display;
        private final String displayPart;

        RoleType(String display, String displayPart) {
            this.display = display;
            this.displayPart = displayPart;
        }


        public String getName() {
            return display;
        }

        public String getNamePart() {
            return displayPart;
        }

        @Override
        public String toString() {
            return display.toLowerCase();
        }
    }
}
