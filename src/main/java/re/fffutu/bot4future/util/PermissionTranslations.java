package re.fffutu.bot4future.util;

import org.javacord.api.entity.permission.PermissionType;

import java.util.HashMap;
import java.util.Map;

public class PermissionTranslations {
    private final static Map<PermissionType, String> translations = new HashMap<>();

    static {
        translations.put(PermissionType.CREATE_INSTANT_INVITE, "Erstellen von sofortigen Einladungen");
        translations.put(PermissionType.KICK_MEMBERS, "Mitglieder kicken");
        translations.put(PermissionType.BAN_MEMBERS, "Mitglieder bannen");
        translations.put(PermissionType.ADMINISTRATOR, "Administrator");
        translations.put(PermissionType.MANAGE_CHANNELS, "Channel verwalten");
        translations.put(PermissionType.MANAGE_SERVER, "Server verwalten");
        translations.put(PermissionType.ADD_REACTIONS, "Reaktionen hinzufügen");
        translations.put(PermissionType.VIEW_AUDIT_LOG, "Einsicht ins Audit-Log");
        translations.put(PermissionType.VIEW_SERVER_INSIGHTS, "Einsicht in die Insights");
        translations.put(PermissionType.READ_MESSAGES, "Nachrichten lesen");
        translations.put(PermissionType.SEND_MESSAGES, "Nachrichten senden");
        translations.put(PermissionType.SEND_TTS_MESSAGES, "TTS-Nachrichten senden");
        translations.put(PermissionType.MANAGE_MESSAGES, "Nachrichten verwalten");
        translations.put(PermissionType.EMBED_LINKS, "Links einbinden");
        translations.put(PermissionType.ATTACH_FILE, "Dateien anhängen");
        translations.put(PermissionType.READ_MESSAGE_HISTORY, "Nachrichtenverlauf lesen");
        translations.put(PermissionType.MENTION_EVERYONE, "@everyone pingen");
        translations.put(PermissionType.USE_EXTERNAL_EMOJIS, "Externe Emojis nutzen");
        translations.put(PermissionType.USE_EXTERNAL_STICKERS, "Externe Sticker nutzen");
        translations.put(PermissionType.CONNECT, "Mit Sprachchat verbinden");
        translations.put(PermissionType.SPEAK, "Im Sprachchat sprechen");
        translations.put(PermissionType.MUTE_MEMBERS, "Mitglieder im Sprachchat muten");
        translations.put(PermissionType.DEAFEN_MEMBERS, "Mitglieder im Sprachchat taub stellen");
        translations.put(PermissionType.MOVE_MEMBERS, "Mitglieder zwischen Sprachchats verschieben");
        translations.put(PermissionType.USE_VOICE_ACTIVITY, "Apps im Sprachchat nutzen");
        translations.put(PermissionType.PRIORITY_SPEAKER, "Very Important Speakwe");
        translations.put(PermissionType.STREAM, "Bildschirm im Sprachchat teilen");
        translations.put(PermissionType.REQUEST_TO_SPEAK, "Sprachanfrage stellen in Stage Channels");
        translations.put(PermissionType.START_EMBEDDED_ACTIVITIES, "Apps im Sprachchat starten");
        translations.put(PermissionType.MANAGE_THREADS, "Threads verwalten");
        translations.put(PermissionType.CREATE_PUBLIC_THREADS, "Öffentliche Threads erstellen");
        translations.put(PermissionType.CREATE_PRIVATE_THREADS, "Private Threads erstellen");
        translations.put(PermissionType.SEND_MESSAGES_IN_THREADS, "Nachrichten in Threads senden");
        translations.put(PermissionType.CHANGE_NICKNAME, "Eigenen Nicknamen ändern");
        translations.put(PermissionType.MANAGE_NICKNAMES, "Fremde Nicknamen verwalten");
        translations.put(PermissionType.MANAGE_ROLES, "Rollen verwalten");
        translations.put(PermissionType.MANAGE_WEBHOOKS, "Webhooks verwalten");
        translations.put(PermissionType.MANAGE_EMOJIS, "Emojis verwalten");
        translations.put(PermissionType.USE_SLASH_COMMANDS, "Slash-Commands nutzen");
    }

    public static String get(PermissionType type) {
        return translations.getOrDefault(type, type.toString());
    }
}
