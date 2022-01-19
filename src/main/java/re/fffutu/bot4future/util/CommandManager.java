package re.fffutu.bot4future.util;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.interaction.ServerSlashCommandPermissionsBuilder;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandPermissionType;
import org.javacord.api.interaction.SlashCommandPermissions;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.EmbedTemplate;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger("command manager");
    private final RoleStore store = Database.ROLES;
    private final Map<String, Command> commands = new HashMap<>();

    public void register() {
        logger.info("Registriere Slash-Commands...");
        DiscordApi api = DiscordBot.INSTANCE.api;

        api.getGlobalSlashCommands().join().stream()
                .filter(cmd -> !commands.containsKey(
                        cmd.getName().toLowerCase()))
                .forEach(cmd -> cmd.deleteGlobal().join());

        api.getServers().forEach(server -> {
            try {
                server.getSlashCommands()
                        .join()
                        .stream()
                        .filter(cmd -> !commands.containsKey(cmd.getName()))
                        .forEach(cmd -> cmd.deleteForServer(server).join());
            } catch (CompletionException e) {
                if (e.getCause() instanceof MissingPermissionsException)
                    logger.warn("Bot hat keinen Zugriff auf Slash-Commands auf dem Server " + server.getName()
                            + " (" + server.getId() +
                            "). Die Slash-Commands konnten nicht erstellt werden.");
                else
                    logger.error("Unbekannter Fehler:", e.getCause());
            }
        });
        logger.info("Alle alten Slash-Commands gelöscht.");

        try {
            api.bulkOverwriteGlobalSlashCommands(commands.values().stream().filter(Command::isGlobal)
                    .map(Command::getBuilder)
                    .collect(Collectors.toList())).join();
        } catch (CompletionException e) {
            logger.info("Error", e);
        }
        api.getServers().forEach(server -> {
            try {
                api.bulkOverwriteServerSlashCommands(server,
                        commands.values()
                                .stream()
                                .filter(Command::isServerOnly)
                                .filter(cmd -> cmd.getAllowedServers()
                                        .isEmpty() ||
                                        cmd.getAllowedServers()
                                                .contains(
                                                        server.getId()))
                                .map(Command::getBuilder)
                                .collect(Collectors.toList()))
                        .join();
                updatePermissions(server, true);
            } catch (Exception e) {
                if (e.getCause() instanceof MissingPermissionsException)
                    logger.warn("Bot hat keinen Zugriff auf Slash-Commands auf dem Server " + server.getName()
                            + " (" + server.getId() +
                            "). Die Slash-Commands konnten nicht erstellt werden.");
                else
                    logger.error("Unbekannter Fehler:", e);
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
                                .setDescription(
                                        "Dieser Befehl sollte nicht exsistieren. Bitte melde dies an einen" +
                                                " der Bot-Entwickler, oder einen Server-Administrator!"))
                        .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                        .respond();
            }
        });
    }

    public void updatePermissions(Server server, boolean silent) {
        List<ServerSlashCommandPermissionsBuilder> builders = new ArrayList<>();
        server.getSlashCommands().thenAccept(cmds -> cmds.forEach(cmd -> {
            List<SlashCommandPermissions> perms = new ArrayList<>();
            if (cmd.getDefaultPermission()) {
                commands.get(cmd.getName()).getDisallowed().forEach(type -> {
                    Optional<Role> optRole = store.getRole(server.getId(), type);
                    if (optRole.isPresent())
                        perms.add(SlashCommandPermissions.create(optRole.get().getId(),
                                SlashCommandPermissionType.ROLE, false));
                });
            } else {
                perms.add(SlashCommandPermissions.create(server.getOwnerId(), SlashCommandPermissionType.USER,
                        true));
                commands.get(cmd.getName()).getAllowed().forEach(type -> {
                    Optional<Role> optRole = store.getRole(server.getId(), type);
                    if (optRole.isPresent())
                        perms.add(SlashCommandPermissions.create(optRole.get().getId(),
                                SlashCommandPermissionType.ROLE, true));
                });
            }
            if (perms.size() != 0)
                builders.add(new ServerSlashCommandPermissionsBuilder(cmd.getId(),
                        perms));
        })).join();
        DiscordBot.INSTANCE.api.batchUpdateSlashCommandPermissions(server, builders).join();
        if (!silent)
            logger.info(
                    "Slash-Command-Permissions für den Server \"" + server.getName() + "\" (" + server.getId() + ") " +
                            "geupdatet.");
    }

    public void updatePermissions(Server server) {
        updatePermissions(server, false);
    }

    public void updatePermissions(long serverId) {
        DiscordBot.INSTANCE.api.getServerById(serverId).ifPresent(this::updatePermissions);
    }

    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }

    public interface Command {
        SlashCommandBuilder getBuilder();

        List<RoleStore.RoleType> getAllowed();

        default List<RoleStore.RoleType> getDisallowed() {
            return List.of();
        }

        ;

        CommandHandler getHandler();

        default boolean isGlobal() {
            return false;
        }

        default boolean isServerOnly() {
            return !isGlobal();
        }

        default List<Long> getAllowedServers() {
            return List.of();
        }
    }

    @FunctionalInterface
    public interface CommandHandler {
        void handle(SlashCommandCreateEvent event);
    }
}
