package re.fffutu.bot4future.util;

import org.simpleyaml.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;

public class ConfigChecker {
    private static final Logger logger = LoggerFactory.getLogger("config checker");

    public static void checkConfigState() {
        Configuration config = DiscordBot.INSTANCE.config;

        if (config.getString("token").equals("your token here")) {
            logger.error("Kein Bot-Token angegeben! Stoppe Bot..");
            System.exit(1);
            return;
        }

        if (config.getLong("dev_server.id") == 0) {
            logger.warn("Es ist keine gültige Dev-Server ID angegeben. Nur dort kann man die aktuellen Logs anfordern" +
                                ".");
        }
        if (config.getLong("dev_server.log_channel") == 0)
            logger.warn("Es ist kein Log-Channel angegeben. Dadurch können die Logs nicht über Discord gesendet " +
                                "werden.");
    }
}
