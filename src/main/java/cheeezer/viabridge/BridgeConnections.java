package cheeezer.viabridge;

import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live registry of the Via {@link UserConnection}s ViaBridge has spliced into NES spectator
 * channels. Populated by the pipeline Mixin on connect and cleared when the channel closes;
 * read by the {@code /nesvia status} command.
 */
public final class BridgeConnections {

    private static final Set<UserConnection> ACTIVE = ConcurrentHashMap.newKeySet();

    private BridgeConnections() {
    }

    public static void add(UserConnection connection) {
        ACTIVE.add(connection);
    }

    public static void remove(UserConnection connection) {
        ACTIVE.remove(connection);
    }

    public static Set<UserConnection> all() {
        return ACTIVE;
    }
}
