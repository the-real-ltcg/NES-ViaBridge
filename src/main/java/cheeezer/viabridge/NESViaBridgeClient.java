package cheeezer.viabridge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Set;

/**
 * Registers {@code /nesvia status} (and the bare {@code /nesvia}), which lists the spectator
 * connections ViaBridge is currently translating and the client version detected for each.
 */
public class NESViaBridgeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("nesvia")
                        .executes(ctx -> {
                            printStatus(ctx.getSource());
                            return 1;
                        })
                        .then(literal("status").executes(ctx -> {
                            printStatus(ctx.getSource());
                            return 1;
                        }))));
    }

    private static void printStatus(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("[NES ViaBridge] host protocol: " + ProtocolVersion.v26_3.getName()
                + " — spectators may join from other versions."));

        Set<UserConnection> connections = BridgeConnections.all();
        if (connections.isEmpty()) {
            source.sendFeedback(Component.literal("No spectator connections are being tracked right now."));
            return;
        }

        source.sendFeedback(Component.literal(connections.size() + " spectator connection(s):"));
        for (UserConnection user : connections) {
            ProtocolInfo info = user.getProtocolInfo();
            String name = info != null && info.getUsername() != null ? info.getUsername() : "<connecting>";
            ProtocolVersion version = info != null ? info.protocolVersion() : null;
            String versionName = version != null ? version.getName() : "negotiating";
            source.sendFeedback(Component.literal(" - " + name + ": " + versionName));
        }
    }

    // Typed brigadier factory pinned to FabricClientCommandSource so the command tree infers
    // correctly (the raw static brigadier helpers default the source type to Object).
    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }
}
