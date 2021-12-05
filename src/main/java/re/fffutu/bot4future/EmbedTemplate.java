package re.fffutu.bot4future;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import re.fffutu.bot4future.db.AuditStore.AuditEntry;
import re.fffutu.bot4future.db.AuditStore.AuditType;

import java.awt.*;
import java.time.Instant;

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

    public static EmbedBuilder auditMessage(AuditEntry entry, Server server) {
        EmbedBuilder builder = info()
                .setTitle("Du wurdest auf dem Server **" + server.getName() + "** " + entry.type.getActionPart())
                .setThumbnail(server.getIcon().orElse(server.getApi().getYourself().getAvatar()));
        if (entry.type != AuditType.WARN)
            builder.addField("Dauer", entry.expiresAt == 0 ? "Permanent" :
                    "Auslauf <t:" + Instant.ofEpochMilli(entry.expiresAt).getEpochSecond() + ":R>");
        String footer = "";
        switch (entry.type) {
            case WARN:
                footer = "Halte dich in Zukunft an die Serverregeln, oder du hast mit " +
                        "härteren Strafen zu rechnen.";
                break;
            case BAN:
                footer = "Mit /entbannung kannst du einen Entbannungsantrag einreichen.";
                break;
            case MUTE:
                footer = "Das Anschreiben von Serverteam-Mitgliedern wird es dir nicht leichter machen, bald wieder " +
                        "auf dem Server schreiben zu können.";
                break;
        }
        return builder.setFooter(footer);
    }
}
