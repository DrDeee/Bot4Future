package re.fffutu.bot4future.util;

import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static long getMillisFromInteraction(SlashCommandInteraction interaction) {
        long minutes = interaction.getOptionLongValueByName("minuten").orElse(0L);
        long hours = interaction.getOptionLongValueByName("stunden").orElse(0L);
        long days = interaction.getOptionLongValueByName("tage").orElse(0L);

        return TimeUnit.MINUTES.toMillis(minutes)
                + TimeUnit.HOURS.toMillis(hours)
                + TimeUnit.DAYS.toMillis(days);
    }
}
