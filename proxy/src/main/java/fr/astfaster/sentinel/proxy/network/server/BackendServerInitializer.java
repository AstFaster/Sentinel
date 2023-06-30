package fr.astfaster.sentinel.proxy.network.server;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.config.Config;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.server.pipeline.FrameDecoder;
import fr.astfaster.sentinel.proxy.network.server.pipeline.FrameEncoder;
import fr.astfaster.sentinel.proxy.network.server.pipeline.PacketCodec;
import fr.astfaster.sentinel.proxy.server.ServerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BackendServerInitializer extends ChannelInitializer<Channel> {

    private final SentinelPlayer player;
    private final ServerImpl server;

    public BackendServerInitializer(SentinelPlayer player, ServerImpl server) {
        this.player = player;
        this.server = server;
    }

    @Override
    protected void initChannel(@NotNull Channel ch) {
        final Config config = Sentinel.instance().config();
        final ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("frame-decoder", new FrameDecoder());
        pipeline.addLast("timeout", new ReadTimeoutHandler(config.readTimeout(), TimeUnit.MILLISECONDS));
        pipeline.addLast("frame-encoder", new FrameEncoder());
        pipeline.addLast("flow-handler", new FlowControlHandler());
        pipeline.addLast("packet-codec", new PacketCodec(this.player.connection().protocolVersion()).reversed());
        pipeline.addLast("packet-handler", new ServerConnectionImpl(ch, this.player, this.server));
    }

}
