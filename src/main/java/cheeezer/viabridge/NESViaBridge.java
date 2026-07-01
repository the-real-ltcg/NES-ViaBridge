package cheeezer.viabridge;

import cheeezer.viabridge.platform.BridgeInjector;
import cheeezer.viabridge.platform.BridgeLoader;
import cheeezer.viabridge.platform.BridgePlatform;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Bootstraps the ViaVersion stack so that Not Enough Spectators can accept spectators running
 * other Minecraft versions. Once initialised, the per-connection Mixin
 * ({@code SpectatorServerNetworkHandlerMixin}) inserts Via's translation handlers into each
 * spectator channel of the NES server pipeline.
 */
public class NESViaBridge implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("nes-viabridge");

    @Override
    public void onInitialize() {
        File dataFolder = FabricLoader.getInstance().getConfigDir().resolve("nes-viabridge").toFile();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            LOGGER.warn("Could not create config directory {}", dataFolder);
        }

        try {
            BridgePlatform platform = new BridgePlatform(dataFolder);
            ViaManagerImpl manager = ViaManagerImpl.builder()
                    .platform(platform)
                    .injector(new BridgeInjector())
                    .loader(new BridgeLoader())
                    .build();
            Via.init(manager);
            manager.init();
            manager.onServerLoaded();
            LOGGER.info("ViaVersion stack initialised (host protocol = 26.2). Spectators on other versions can now connect.");
        } catch (Throwable t) {
            LOGGER.error("Failed to initialise the ViaVersion stack; cross-version spectators will be unavailable", t);
        }
    }
}
