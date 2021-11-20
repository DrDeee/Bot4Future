package re.fffutu.bot4future.logging;

import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;

public class EventAuditLogButtonTemplates {
    public static Button DELETE(long channelId, long msgId) {
        return new ButtonBuilder()
                .setLabel("Löschen")
                .setCustomId("msg-delete:" + channelId + ":" + msgId)
                .setStyle(ButtonStyle.DANGER)
                .build();
    }

    public static Button DELETE_DISABLED() {
        return new ButtonBuilder()
                .setLabel("Löschen")
                .setStyle(ButtonStyle.DANGER)
                .setDisabled(true)
                .setCustomId("ignorethis")
                .build();
    }

    public static Button MESSAGE_LINK(String url) {
        return new ButtonBuilder()
                .setLabel("Zur Nachricht")
                .setUrl(url)
                .setStyle(ButtonStyle.LINK)
                .build();
    }

    public static Button MESSAGE_LINK_DISABLED() {
        return new ButtonBuilder()
                .setLabel("Zur Nachricht")
                .setStyle(ButtonStyle.LINK)
                .setUrl("https://doneveruse.this")
                .setDisabled(true)
                .build();
    }

    public static Button DETAILS(long channelId, long msgId) {
        return new ButtonBuilder()
                .setLabel("Details")
                .setCustomId("msg-details:open:" + channelId + ":" + msgId)
                .setStyle(ButtonStyle.PRIMARY)
                .build();
    }

    public static Button YES(long channelId, long msgId) {
        return new ButtonBuilder()
                .setLabel("Ja")
                .setCustomId("confirm-delete:" + channelId + ":" + msgId)
                .setStyle(ButtonStyle.SUCCESS)
                .build();
    }

    public static Button NO(long channelId, long msgId) {
        return new ButtonBuilder()
                .setLabel("Nein")
                .setCustomId("abort-delete:" + channelId + ":" + msgId)
                .setStyle(ButtonStyle.DANGER)
                .build();
    }

    public static Button NEXT() {
        return new ButtonBuilder()
                .setLabel("Nächste Version")
                .setCustomId("msg-details:next")
                .setStyle(ButtonStyle.PRIMARY)
                .build();
    }

    public static Button BACK() {
        return new ButtonBuilder()
                .setLabel("Vorherige Version")
                .setCustomId("msg-details:back")
                .setStyle(ButtonStyle.PRIMARY)
                .build();
    }

    public static Button NEXT_DISABLED() {
        return new ButtonBuilder().copy(NEXT()).setDisabled(true).build();
    }

    public static Button BACK_DISABLED() {
        return new ButtonBuilder().copy(BACK()).setDisabled(true).build();
    }

    public static Button DETAILS_DELETE() {
        return new ButtonBuilder()
                .setLabel("Löschen")
                .setCustomId("msg-details:delete")
                .setStyle(ButtonStyle.DANGER)
                .build();
    }

    public static Button DETAILS_DELETE_DISABLED() {
        return new ButtonBuilder()
                .setLabel("Löschen")
                .setStyle(ButtonStyle.DANGER)
                .setDisabled(true)
                .setCustomId("ignorethis")
                .build();
    }

    public static Button DETAILS_YES() {
        return new ButtonBuilder()
                .setLabel("Ja")
                .setStyle(ButtonStyle.SUCCESS)
                .setCustomId("msg-details:yes")
                .build();
    }

    public static Button DETAILS_NO() {
        return new ButtonBuilder()
                .setLabel("Nein")
                .setStyle(ButtonStyle.DANGER)
                .setCustomId("msg-details:no")
                .build();
    }
}
