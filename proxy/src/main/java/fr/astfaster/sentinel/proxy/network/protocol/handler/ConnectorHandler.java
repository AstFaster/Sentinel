package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.api.network.protocol.HandshakeInfo;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.api.player.GameProfile;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.server.Server;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Disconnect;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Handshake;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginStart;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginSuccess;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.JoinGame;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.Respawn;
import fr.astfaster.sentinel.proxy.player.SentinelPlayerImpl;
import fr.astfaster.sentinel.proxy.server.ServerImpl;
import fr.astfaster.sentinel.proxy.util.Serializers;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectorHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(ConnectorHandler.class);

    private final ServerConnectionImpl connection;
    private final PlayerConnectionImpl playerConnection;
    private final SentinelPlayerImpl player;
    private final ServerImpl server;

    public ConnectorHandler(ServerConnectionImpl connection) {
        this.connection = connection;
        this.player = (SentinelPlayerImpl) this.connection.player();
        this.playerConnection = (PlayerConnectionImpl) this.player.connection();
        this.server = this.connection.server();
    }

    @Override
    public void connected(Channel channel) {
        // We are connected to the server, now we have to init the protocol
        final HandshakeInfo handshake = this.playerConnection.handshake();

        String address = handshake.address();

        if (Sentinel.instance().config().ipForwarding() && this.playerConnection.remoteAddress() instanceof InetSocketAddress) {
            address += "\00" + this.sanitizeAddress((InetSocketAddress) this.playerConnection.remoteAddress()) + "\00" + this.player.uuid();

            final GameProfile profile = this.player.profile();
            final List<GameProfile.Property> properties = new ArrayList<>(profile.properties());

            if (properties.size() > 0) {
                address += "\00" + Serializers.GLOBAL_SERIALIZER.toJson(properties);
            }
        } else if (handshake.extraData() != null) { // Re-add extra handshake data (if not null)
            address = handshake.address() + handshake.extraData();
        }

        this.connection.sendPacket(new Handshake(handshake.protocol(), address, handshake.port(), ProtocolState.LOGIN_ID));
        this.connection.protocolState(ProtocolState.LOGIN);
        this.connection.sendPacket(new LoginStart(this.player.name()));
    }

    private String sanitizeAddress(InetSocketAddress address) {
        final String addressStr = address.getAddress().getHostAddress();

        // Try to remove IPv6 scope
        if (address.getAddress() instanceof Inet6Address) {
            final int strip = addressStr.indexOf('%');

            return (strip == -1) ? addressStr : addressStr.substring(0, strip);
        }
        return addressStr;
    }

    @Override
    public void handle(Packet packet) {
        if (packet instanceof final Disconnect disconnect) {
            this.player.disconnect(disconnect.reason());
        } else if (packet instanceof LoginSuccess) {
            this.connection.protocolState(ProtocolState.PLAY);
        } else if (packet instanceof final JoinGame join) {
            this.playerConnection.sendPacket(join);

            // Check if the player was already connected to a server before
            if (this.player.serverConnection() != null) {
                // Disconnect first
                this.playerConnection.sendPacket(Respawn.of(join));
                this.player.serverConnection().disconnect();

                if (this.playerConnection.protocolVersion().less(ProtocolVersion.V_1_16)) {
                    join.dimension(join.dimension() == 0 ? -1 : 0);
                }
            }

            this.connection.handler(new DownstreamHandler(this.connection, this.player, this.server));
            this.server.addPlayer(this.player);
            this.player.serverConnection(this.connection);

            if (Sentinel.instance().config().logs().logServersConnections()) {
                LOGGER.info("{} has connected", this);
            }
        }
    }

    @Override
    public String toString() {
        return "[" + this.connection.remoteAddress().toString() + "|" + this.player.name() + " <-> Server Connector <-> " + this.server.name() + "]";
    }

}
