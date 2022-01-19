package re.fffutu.bot4future.util;

import org.javacord.api.DiscordApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.fffutu.bot4future.db.AuditStore.AuditEntry;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.db.TimedTaskStore;
import re.fffutu.bot4future.db.TimedTaskStore.TimedTask;
import re.fffutu.bot4future.logging.AuditLogger;

import java.util.*;

public class TimedTaskManager {
    private final Logger logger = LoggerFactory.getLogger("timed task manager");
    private final Timer timer = new Timer();
    private final TimedTaskStore store = new TimedTaskStore();
    private DiscordApi api;

    private Map<UUID, TimedTask> pendingTasks = new HashMap<>();

    public void init(DiscordApi api) {
        this.api = api;
        store.getTimedTasks().forEach(task -> {
            scheduleTask(task, false);
        });
        logger.info(pendingTasks.size() + " Task(s) geplant.");
    }

    public void scheduleTask(TimedTask task, boolean save) {
        if (save) store.saveTimedTask(task);
        pendingTasks.put(task.id, task);
        if (task.time <= System.currentTimeMillis())
            new TimerTaskImpl(this, task).run();
        else
            timer.schedule(new TimerTaskImpl(this, task), new Date(task.time));
    }

    public void scheduleTask(TimedTask task) {
        scheduleTask(task, true);
    }

    public void scheduleTask(AuditEntry auditEntry) {
        TimedTask task = new TimedTask();
        task.relatedAudit = auditEntry.id;
        task.time = auditEntry.expiresAt;
        task.type = auditEntry.type.getTimedTaskType();
        scheduleTask(task);
    }

    private void finishTask(UUID id) {
        pendingTasks.remove(id);
        store.deleteTimedTask(id);
    }

    private static class TimerTaskImpl extends TimerTask {
        private TimedTask task;
        private TimedTaskManager manager;

        public TimerTaskImpl(TimedTaskManager manager, TimedTask task) {
            this.manager = manager;
            this.task = task;
        }

        @Override
        public void run() {
            System.out.println("Task runned");
            manager.finishTask(task.id);
            AuditEntry entry = Database.AUDIT.getAuditEntryById(task.relatedAudit).orElse(null);
            if (entry == null) return;
            switch (task.type) {
                case UNBAN: {
                    manager.api.getServerById(entry.serverId).ifPresent(server -> {
                        server.unbanUser(entry.userId);
                        AuditLogger.logAuditEnd(server, entry);
                    });
                    break;
                }
                case UNMUTE: {
                    manager.finishTask(task.id);
                    Database.ROLES.getRole(entry.serverId, RoleType.MUTED).ifPresent(mutedRole -> {
                        manager.api.getUserById(entry.userId).thenAccept(user -> {
                            AuditLogger.logAuditEnd(mutedRole.getServer(), entry);
                            mutedRole.removeUser(user);
                            entry.roles.forEach(roleId -> {
                                manager.api.getRoleById(roleId).ifPresent(role -> user.addRole(role));
                            });
                        });
                    });
                }
            }
        }
    }
}
