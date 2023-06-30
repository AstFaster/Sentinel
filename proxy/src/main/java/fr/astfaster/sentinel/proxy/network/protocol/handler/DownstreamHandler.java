package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.server.Server;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.JoinGame;
import fr.astfaster.sentinel.proxy.player.SentinelPlayerImpl;
import fr.astfaster.sentinel.proxy.server.ServerImpl;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DownstreamHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(DownstreamHandler.class);

    private final ServerConnectionImpl connection;
    private final PlayerConnectionImpl playerConnection;

    private final SentinelPlayerImpl player;
    private final ServerImpl server;

    public DownstreamHandler(ServerConnectionImpl connection, SentinelPlayerImpl player, ServerImpl server) {
        this.connection = connection;
        this.player = player;
        this.server = server;
        this.playerConnection = (PlayerConnectionImpl) this.player.connection();
    }

    @Override
    public void disconnected(Channel channel) {
        this.server.removePlayer(this.player.uuid());
        this.player.serverConnection(null);

        if (Sentinel.instance().config().logs().logServersDisconnections()) {
            LOGGER.info("{} has disconnected", this);
        }
    }

    @Override
    public void handle(PacketWrapper wrapper) {
        // Sends packet to upstream connection
        this.playerConnection.sendPacket(wrapper);
    }

    @Override
    public void handle(Packet packet) {
        // Sends packet to upstream connection
        this.playerConnection.sendPacket(packet);
    }

    @Override
    public String toString() {
        return "[" + this.server.name() + " <-> Downstream Connection <-> " + this.playerConnection.remoteAddress().toString() + "|" + this.player.name() + "]";
    }

}
