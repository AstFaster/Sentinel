package fr.astfaster.sentinel.proxy.network.connection;

import fr.astfaster.sentinel.api.network.connection.ServerConnection;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.proxy.network.protocol.handler.ConnectorHandler;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import fr.astfaster.sentinel.proxy.server.ServerImpl;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

public class ServerConnectionImpl extends DefaultConnection implements ServerConnection {

    private final SentinelPlayer player;
    private final ServerImpl server;

    public ServerConnectionImpl(Channel channel, SentinelPlayer player, ServerImpl server) {
        super(channel);
        this.player = player;
        this.server = server;

        this.handler(new ConnectorHandler(this));
    }

    @Override
    public void sendPacket(Packet packet) {
        super.sendPacket(packet);
    }

    @Override
    public void sendPacket(PacketWrapper wrapper) {
        super.sendPacket(wrapper);
    }

    public SentinelPlayer player() {
        return this.player;
    }

    @Override
    public ServerImpl server() {
        return this.server;
    }

    @Override
    public void disconnect(Component reason) {
        this.channel.flush();
        this.channel.close();
    }

}
