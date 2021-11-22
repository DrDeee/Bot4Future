package re.fffutu.bot4future;

import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class EmbedTemplate {
    public static EmbedBuilder base() {
        return new EmbedBuilder();
    }

    public static EmbedBuilder info() {
        return base()
                .setColor(new Color(144, 211, 237));
    }

    public static EmbedBuilder success() {
        return base()
                .setColor(new Color(29, 166, 74));
    }

    public static EmbedBuilder error() {
        return base()
                .setColor(new Color(245, 51, 63));
    }
}
