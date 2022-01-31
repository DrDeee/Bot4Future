package re.fffutu.bot4future.util;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;

@FunctionalInterface
public interface SubcommandHandler {
    void handle(List<SlashCommandInteractionOption> option, SlashCommandInteraction interaction);
}
