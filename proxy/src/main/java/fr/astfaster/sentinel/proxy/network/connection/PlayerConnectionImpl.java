package fr.astfaster.sentinel.proxy.network.connection;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.api.network.protocol.HandshakeInfo;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.proxy.SentinelServer;
import fr.astfaster.sentinel.proxy.event.impl.DisconnectEventImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.proxy.network.protocol.handler.HandshakeHandler;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Disconnect;
import fr.astfaster.sentinel.proxy.network.server.pipeline.PacketCodec;
import fr.astfaster.sentinel.proxy.util.Serializers;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PlayerConnectionImpl extends DefaultConnection implements PlayerConnection {

    private HandshakeInfo handshake;
    private Supplier<SocketAddress> virtualHost;

    private ProtocolVersion protocolVersion = ProtocolVersion.UNKNOWN;

    private SentinelPlayer player;

    public PlayerConnectionImpl(Channel channel) {
        super(channel);
        super.handler(new HandshakeHandler(this));
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        super.channelInactive(ctx);

        if (this.player != null) {
            Sentinel.instance().eventBus().publish(new DisconnectEventImpl(this.player))
                    .thenAccept(event -> SentinelServer.instance().unregisterPlayer(this.player));
        }
    }

    @Override
    public void disconnect(Component reason) {
        if (this.channel.isActive()) {
            if (this.protocolState == ProtocolState.LOGIN) {
                this.channel.eventLoop().schedule(() -> super.close(reason == null ? null : new Disconnect(Serializers.MESSAGE_SERIALIZER.toJson(reason))), 250, TimeUnit.MILLISECONDS);
            } else if (this.protocolState == ProtocolState.PLAY){
                this.channel.eventLoop().execute(() -> super.close(reason == null ? null : new Disconnect(Serializers.MESSAGE_SERIALIZER.toJson(reason))));
            } else {
                this.channel.eventLoop().execute(() -> super.close(null));
            }

            if (reason != null && Sentinel.instance().config().logs().logDisconnections()) {
                this.logger.info("{} has disconnected with: {}", this.handler, PlainTextComponentSerializer.plainText().serialize(reason));

                this.handler(null);
            }
        }
    }

    @Override
    public void disconnect() {
        this.disconnect(null);
    }

    @Override
    public HandshakeInfo handshake() {
        return this.handshake;
    }

    public void handshake(HandshakeInfo handshake) {
        this.handshake = handshake;
    }

    @Override
    public Supplier<SocketAddress> virtualHost() {
        return virtualHost;
    }

    public void virtualHost(Supplier<SocketAddress> virtualHost) {
        this.virtualHost = virtualHost;
    }

    public void protocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.channel.pipeline().get(PacketCodec.class).protocolVersion(protocolVersion);
    }

    @Override
    public ProtocolVersion protocolVersion() {
        return this.protocolVersion;
    }

    public SentinelPlayer player() {
        return this.player;
    }

    public void player(SentinelPlayer player) {
        this.player = player;
    }

}
