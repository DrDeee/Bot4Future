package re.fffutu.bot4future;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.config.ConfigCommand;
import re.fffutu.bot4future.db.ChannelStore;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.logging.EventAuditListener;
import re.fffutu.bot4future.logging.UserLogListener;
import re.fffutu.bot4future.logging.actions.MessageDeleteActionListener;
import re.fffutu.bot4future.logging.actions.MessageDetailsActionListener;
import re.fffutu.bot4future.moderation.BanCommand;
import re.fffutu.bot4future.util.CommandManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordBot {
    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5);


    public static DiscordBot INSTANCE;
    public DiscordApi api;
    public Configuration config;

    public CommandManager commandManager = new CommandManager();

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

        new RoleStore().setRole(691691563805573121L, RoleStore.RoleType.ADMINISTRATOR, 729314493074505758L);
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

            //ADD COMMANDS

            // moderation
            commandManager.addCommand("ban", new BanCommand());

            // configuration
            commandManager.addCommand("config", new ConfigCommand());

            commandManager.register();

            api.addMessageCreateListener(e -> {
                if (e.getMessageContent().equals("-here")) {
                    ChannelStore.setChannel(e.getServer().get().getId(), e.getChannel().getId(), ChannelStore.ChannelType.MESSAGE_LOG);
                }
                if (e.getMessageContent().equals("-here2")) {
                    ChannelStore.setChannel(e.getServer().get().getId(), e.getChannel().getId(), ChannelStore.ChannelType.STORE);
                }
                if (e.getMessageContent().equals("-here3")) {
                    ChannelStore.setChannel(e.getServer().get().getId(), e.getChannel().getId(), ChannelStore.ChannelType.USER_LOG);
                }
            });
        });
    }
}
