package re.fffutu.bot4future.db;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import redis.clients.jedis.Jedis;

public class CookieStore {
    public int getCookiesOf(Server server, User user) {
        Jedis jedis = Database.create();
        String cookiesRaw = jedis.get("cookies:" + server.getId() + ":" + user.getId());
        if (cookiesRaw == null) return 0;
        try {
            return Integer.parseInt(cookiesRaw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setCookiesOf(Server server, User user, int cookies) {
        Jedis jedis = Database.create();
        jedis.set("cookies:" + server.getId() + ":" + user.getId(), cookies + "");
    }
}
