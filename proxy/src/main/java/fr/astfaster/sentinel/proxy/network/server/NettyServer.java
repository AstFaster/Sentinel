package fr.astfaster.sentinel.proxy.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

import static fr.astfaster.sentinel.proxy.network.ConnectionManager.WRITE_MARK;

public class NettyServer {

    private final Logger logger = LogManager.getLogger(NettyServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;

    private final InetSocketAddress address;

    public NettyServer(InetSocketAddress address) {
        this.address = address;
    }

    public void start() {
        final Transport transport = Transport.best();

        this.bossGroup = transport.newEventLoop(Transport.Type.BOSS);
        this.workerGroup = transport.newEventLoop(Transport.Type.WORKER);

        final ServerBootstrap bootstrap = new ServerBootstrap()
                .channel(transport.serverChannel())
                .group(this.bossGroup, this.workerGroup)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WRITE_MARK)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 0x18)
                .childHandler(new ServerInitializer())
                .localAddress(this.address);

        if (transport == Transport.EPOLL) {
            bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, true);
        }

        bootstrap.bind().addListener((ChannelFutureListener) future -> {
            this.channel = future.channel();

           if (future.isSuccess()) {
               this.logger.info("Listening for incoming connections on {}", this.channel.localAddress());
           } else {
               this.logger.error("Couldn't bind server to {}!", this.address, future.cause());
           }
        });
    }

    public void stop() {
        this.channel.close().syncUninterruptibly();
    }

    public InetSocketAddress address() {
        return this.address;
    }

    public EventLoopGroup workerGroup() {
        return this.workerGroup;
    }
}
