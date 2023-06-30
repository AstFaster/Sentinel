package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Supplier;

public class UpstreamHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(UpstreamHandler.class);

    private final Supplier<Optional<ServerConnectionImpl>> serverConnection;

    private final PlayerConnectionImpl connection;
    private final SentinelPlayer player;

    public UpstreamHandler(PlayerConnectionImpl connection) {
        this.connection = connection;
        this.player = this.connection.player();
        this.serverConnection = () -> Optional.ofNullable((ServerConnectionImpl) this.player.serverConnection());
    }

    @Override
    public void disconnected(Channel channel) {
        if (Sentinel.instance().config().logs().logDisconnections()) {
            LOGGER.info("{} has disconnected", this);

            this.serverConnection.get().ifPresent(connection -> connection.channel().close());
        }
    }

    @Override
    public void handle(PacketWrapper wrapper) {
        // Send packet to downstream connection
        this.serverConnection.get().ifPresent(connection -> {
            if (connection.protocolState() == ProtocolState.PLAY) {
                connection.sendPacket(wrapper);
            }
        });
    }

    @Override
    public void handle(Packet packet) {
        // Send packet to downstream connection
        this.serverConnection.get().ifPresent(connection -> {
            if (connection.protocolState() == ProtocolState.PLAY) {
                connection.sendPacket(packet);
            }
        });
    }

    @Override
    public String toString() {
        return "[" + this.connection.remoteAddress().toString() + "|" + this.player.name() + " -> Upstream Connection]";
    }

}
