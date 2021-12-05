package re.fffutu.bot4future;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.config.ConfigCommand;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.logging.EventAuditListener;
import re.fffutu.bot4future.logging.ServerLogListener;
import re.fffutu.bot4future.logging.UserLogListener;
import re.fffutu.bot4future.logging.actions.MessageDeleteActionListener;
import re.fffutu.bot4future.logging.actions.MessageDetailsActionListener;
import re.fffutu.bot4future.moderation.*;
import re.fffutu.bot4future.util.CommandManager;
import re.fffutu.bot4future.util.TimedTaskManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordBot {
    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5);


    public static DiscordBot INSTANCE;
    public DiscordApi api;
    public Configuration config;

    public CommandManager commandManager = new CommandManager();
    public TimedTaskManager timedTaskManager = new TimedTaskManager();

    private Logger logger = LoggerFactory.getLogger("main");

    public DiscordBot() {
        INSTANCE = this;
        loadConfig();
        Database.init();
        start();
    }

    public static void main(String[] args) {
        new DiscordBot();
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

        logger.info("Anmelden bei Discord..");
        builder.login().thenAccept(discordApi -> {
            this.api = discordApi;
            logger.info("Discord Bot Online");

            //ADD LISTENERS

            // logging
            api.addListener(new EventAuditListener());
            api.addListener(new MessageDeleteActionListener());
            api.addListener(new MessageDetailsActionListener());

            api.addListener(new UserLogListener());
            api.addListener(new ServerLogListener());

            // moderation
            api.addListener(new ModerationListener());

            //ADD COMMANDS

            // moderation
            commandManager.addCommand("ban", new BanCommand());
            commandManager.addCommand("warn", new WarnCommand());
            commandManager.addCommand("mute", new MuteCommand());

            // configuration
            commandManager.addCommand("config", new ConfigCommand());
            commandManager.addCommand("userinfo", new UserinfoCommand());

            commandManager.register();

            timedTaskManager.init(api);
        });
    }
}
