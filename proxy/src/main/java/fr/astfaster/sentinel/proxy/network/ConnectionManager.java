package fr.astfaster.sentinel.proxy.network;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.config.ServerBindings;
import fr.astfaster.sentinel.proxy.network.server.NettyServer;
import io.netty.channel.WriteBufferWaterMark;

import java.net.InetSocketAddress;

public class ConnectionManager {

    public static final WriteBufferWaterMark WRITE_MARK = new WriteBufferWaterMark(1 << 20, 1 << 21);

    private NettyServer server;

    public void init() {
        final ServerBindings bindings = Sentinel.instance().config().serverBindings();

        this.server = new NettyServer(new InetSocketAddress(bindings.hostAddress(), bindings.port()));
        this.server.start();
    }

    public NettyServer server() {
        return this.server;
    }

    public void shutdown() {
        this.server.stop();
    }

}
