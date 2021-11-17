package re.fffutu.bot4future;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.logging.EventLogListener;

public class DiscordBot {
    public static DiscordBot INSTANCE;
    public DiscordApi api;
    public Configuration config;

    private Logger logger = LoggerFactory.getLogger("main");

    public static void main(String[] args) {
        new DiscordBot();
    }

    public DiscordBot() {
        INSTANCE = this;
        loadConfig();
        Database.init();
        start();
    }

    private void loadConfig() {
        YamlFile config = new YamlFile("data/config.yaml");
        this.config = config;
        try {
            if (!config.exists()) {
                logger.info("Neue Konfigurationsdatei erstellt: " + config.getFilePath() + "\n");
                config.createNewFile(true);
            } else {
                logger.info(config.getFilePath() + " exsistiert, lade Konfiguration...\n");
            }
            config.load();

            config.addDefault("token", "your token here");
            config.addDefault("devs", new long[]{111111111, 222222222, 333333333});
            config.addDefault("redis.hostname", "localhost");
            config.addDefault("redis.port", 6379);

            config.save();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        if (config.getString("token").equals("your token here")) {
            logger.error("Kein Bot-Token angegeben! Stoppe Bot..");
            System.exit(1);
            return;
        }
        DiscordApiBuilder builder = new DiscordApiBuilder();

        builder.setToken(config.getString("token"));
        builder.setAllIntents();

        builder.login().thenAccept(discordApi -> {
            this.api = discordApi;
            logger.info("Discord Bot Online");

            api.addListener(new EventLogListener());
        });
    }
}
