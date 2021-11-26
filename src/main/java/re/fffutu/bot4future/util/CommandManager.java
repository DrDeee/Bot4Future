package re.fffutu.bot4future.util;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.util.logging.ExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.RoleStore;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger("command manager");
    private Map<String, Command> commands = new HashMap<>();

    private final RoleStore store = new RoleStore();

    public void register(boolean clean) {
        DiscordApi api = DiscordBot.INSTANCE.api;

        if (clean) {
            api.getGlobalSlashCommands().exceptionally(ExceptionLogger.get()).join().forEach(cmd -> cmd.deleteGlobal().exceptionally(ExceptionLogger.get()).join());
            api.getServers().forEach(server -> {

                try {
                    List<SlashCommand> cmds = server.getSlashCommands().join();
                    cmds.forEach(cmd -> cmd.deleteForServer(server).exceptionally(ExceptionLogger.get()).join());
                } catch (CompletionException e) {
                    if (e.getCause() instanceof MissingPermissionsException)
                        logger.warn("Bot hat keinen Zugriff auf Slash-Commands auf dem Server " + server.getName()
                                + " (" + server.getId() + "). Die Slash-Commands konnten nicht gelöscht werden.");
                    else logger.error("Unbekannter Fehler:", e.getCause());
                }
            });
            logger.info("Alle Slash-Commands gelöscht.");
        }
        api.bulkOverwriteGlobalSlashCommands(commands.values().
                stream().filter(Command::isGlobal)
                .map(Command::getBuilder)
                .collect(Collectors.toList())).join();
        api.getServers().forEach(server -> {
            try {
                List<ServerSlashCommandPermissionsBuilder> builders = new ArrayList<>();

                api.bulkOverwriteServerSlashCommands(server,
                        commands.values().stream()
                                .filter(Command::isServerOnly).map(Command::getBuilder).collect(Collectors.toList())).join().forEach(cmd -> {
                    if (cmd.getDefaultPermission()) return;
                    List<SlashCommandPermissions> perms = new ArrayList<>();
                    perms.add(SlashCommandPermissions.create(server.getOwnerId(), SlashCommandPermissionType.USER,
                            true));
                    commands.get(cmd.getName()).getAllowed().forEach(type -> {
                        Optional<Role> optRole = store.getRole(server.getId(), type);
                        if (optRole.isPresent())
                            perms.add(SlashCommandPermissions.create(optRole.get().getId(),
                                    SlashCommandPermissionType.ROLE, true));
                    });
                    builders.add(new ServerSlashCommandPermissionsBuilder(cmd.getId(),
                            perms));
                });
                api.batchUpdateSlashCommandPermissions(server, builders).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof MissingPermissionsException)
                    logger.warn("Bot hat keinen Zugriff auf Slash-Commands auf dem Server " + server.getName()
                            + " (" + server.getId() + "). Die Slash-Commands konnten nicht erstellt werden.");
                else logger.error("Unbekannter Fehler:", e.getCause());
            }
        });
        logger.info("Alle Slash-Commands registriert bzw. geupdated.");
        api.addSlashCommandCreateListener(event -> {
            if (commands.containsKey(event.getSlashCommandInteraction().getCommandName()))
                commands.get(event.getSlashCommandInteraction().getCommandName()).getHandler().handle(event);
            else {
                logger.warn("Für den Befehl " + event.getSlashCommandInteraction().getCommandName() + " ist " +
                        "kein Handler registriert.");
                event.getInteraction().createImmediateResponder()
                        .addEmbed(EmbedTemplate.error()
                                .setDescription("Dieser Befehl sollte nicht exsistieren. Bitte melde dies an einen" +
                                        " der Bot-Entwickler, oder einen Server-Administrator!"))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
            }
        });
    }

    public void register() {
        register(false);
    }

    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }

    public interface Command {
        SlashCommandBuilder getBuilder();

        List<RoleStore.RoleType> getAllowed();

        CommandHandler getHandler();

        default boolean isGlobal() {
            return false;
        }

        default boolean isServerOnly() {
            return !isGlobal();
        }
    }

    @FunctionalInterface
    public interface CommandHandler {
        void handle(SlashCommandCreateEvent event);
    }
}
