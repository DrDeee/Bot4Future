package re.fffutu.bot4future.util;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SubcommandManager {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, SubcommandHandler> handlers = new HashMap<>();

    public void addHandler(String name, SubcommandHandler handler) {
        handlers.put(name, handler);
    }

    public void handle(SlashCommandInteractionOption option , SlashCommandInteraction interaction) {
        if (!option.isSubcommandOrGroup()) {
            logger.warn("Die gegebene Option war kein Subcommand/keine Gruppe!");
            return;
        }
        String name = option.getName();
        if (handlers.containsKey(name)) handlers.get(name).handle(option.getOptions(), interaction);
    }
}
