package re.fffutu.bot4future.util;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import re.fffutu.bot4future.DiscordBot;
import re.fffutu.bot4future.db.Database;
import re.fffutu.bot4future.db.RoleStore.RoleType;
import re.fffutu.bot4future.db.ServerStore;
import re.fffutu.bot4future.logging.UserLogListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InviteManager {
    private final Map<Long, ServerInviteTracker> trackers = new HashMap<>();

    private final ServerMemberJoinListener handler = (event) -> {
        if (trackers.containsKey(event.getServer().getId())) {
            ServerInviteTracker tracker = trackers.get(event.getServer().getId());
            if (tracker.checkEvent(event)) {
                Database.ROLES.getRole(event.getServer().getId(), RoleType.AUTO_ROLE).ifPresent(role -> {
                    event.getUser().addRole(role).join();
                    UserLogListener.INSTANCE.logAutoJoin(event.getServer(), event.getUser());
                });
            }
        }
    };

    public ServerMemberJoinListener getHandler() {
        return handler;
    }

    public void init() {
        DiscordBot.INSTANCE.api.getServers().forEach(server -> {
            String code = Database.SERVERS.getAttribute(server.getId(), ServerStore.INVITE_CODE);
            if (code != null) setInvite(server, code);
        });
    }

    public void setInvite(Server server, String code) {
        if (!trackers.containsKey(server.getId()))
            trackers.put(server.getId(), new ServerInviteTracker(server, code));
        else {
            trackers.get(server.getId()).setInviteCode(code);
        }
    }


    class ServerInviteTracker {
        private Server server;
        private String inviteCode;

        private int inviteCount = 0;

        public ServerInviteTracker(Server server, String inviteCode) {
            this.server = server;
            this.inviteCode = inviteCode;

            loadInviteCount();
        }

        public void setInviteCode(String newInviteCode) {
            this.inviteCode = newInviteCode;
            this.inviteCount = 0;
            loadInviteCount();
        }

        private void loadInviteCount() {
            this.server.getInvites()
                       .thenAccept(
                               invites -> invites.stream()
                                                 .filter(invite -> invite.getCode().equalsIgnoreCase(inviteCode))
                                                 .findAny()
                                                 .ifPresent(invite -> this.inviteCount = invite.getUses()));
        }

        public boolean checkEvent(ServerMemberJoinEvent event) {
            Optional<RichInvite> inviteOptional = event.getServer()
                                                       .getInvites()
                                                       .join()
                                                       .stream()
                                                       .filter(invite -> invite.getCode().equalsIgnoreCase(inviteCode))
                                                       .findAny();
            if (!inviteOptional.isPresent()) return false;
            if (inviteCount < inviteOptional.get().getUses()) {
                inviteCount++;
                return true;
            } else return false;
        }
    }
}
