package cheeezer.viabridge.mixin;

import cheeezer.viabridge.BridgeConnections;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import com.viaversion.viaversion.platform.ViaDecodeHandler;
import com.viaversion.viaversion.platform.ViaEncodeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inserts ViaVersion's translation handlers into each spectator connection of the NES server.
 *
 * <p>Targeted by string so this companion mod does not need NES on its compile classpath; it
 * applies at runtime when NES is installed. NES sets up its pipeline (timeout, splitter,
 * flowcontrol, decoder, prepender, outbound_config, handler) before {@code channelActive} fires,
 * so this is the right moment to splice Via in.</p>
 *
 * <p>This is the standard "backend server" injection (mirroring Via's Bukkit handlers): a
 * server-side {@link UserConnection} is created and the Via decode/encode handlers translate
 * between the spectator's protocol version and the host's native 26.2 protocol.</p>
 */
@Mixin(targets = "cheeezer.notenoughspectators.server.SpectatorServerNetworkHandler", remap = false)
public class SpectatorServerNetworkHandlerMixin {

    @Inject(method = "channelActive(Lio/netty/channel/ChannelHandlerContext;)V", at = @At("TAIL"), remap = false)
    private void nesviabridge$injectVia(ChannelHandlerContext context, CallbackInfo ci) {
        Channel channel = context.channel();
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(ViaDecodeHandler.NAME) != null) {
            return; // already injected for this channel
        }

        // serverSide = false -> this connection is treated as a backend connection that translates
        // the spectator's (client) version to/from the host's native version reported by BridgeInjector.
        UserConnection user = ViaChannelInitializer.createUserConnection(channel, false);

        // The Minecraft encoder is named "encoder" once a protocol is bound; before that NES has an
        // "outbound_config" placeholder in its place. Insert the Via encoder just before whichever exists.
        String encoderName = pipeline.get("outbound_config") != null ? "outbound_config" : "encoder";
        pipeline.addBefore(encoderName, ViaEncodeHandler.NAME, new ViaEncodeHandler(user));
        pipeline.addBefore("decoder", ViaDecodeHandler.NAME, new ViaDecodeHandler(user));

        // Track the connection for /nesvia status, and forget it when the channel closes.
        BridgeConnections.add(user);
        channel.closeFuture().addListener(future -> BridgeConnections.remove(user));
    }
}
