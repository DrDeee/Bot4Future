package re.fffutu.bot4future.moderation;

import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import re.fffutu.bot4future.db.AuditStore;
import re.fffutu.bot4future.db.AuditStore.AuditEntryFilter;
import re.fffutu.bot4future.db.AuditStore.AuditType;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore;
import re.fffutu.bot4future.db.RoleStore.RoleType;

public class ModerationListener implements ServerMemberJoinListener {
    private final AuditStore auditStore = Database.AUDIT;
    private final RoleStore roleStore = Database.ROLES;

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        if (new AuditEntryFilter(auditStore.getAuditEntries(event.getServer().getId(), event.getUser().getId())).ofType(
                AuditType.MUTE).actualOnly().get().size() != 0) {
            roleStore.getRole(event.getServer().getId(), RoleType.MUTED)
                     .ifPresent(role -> role.addUser(event.getUser()));
        }
    }
}
