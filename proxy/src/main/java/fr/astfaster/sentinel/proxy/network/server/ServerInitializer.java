package fr.astfaster.sentinel.proxy.network.server;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.config.Config;
import fr.astfaster.sentinel.api.event.impl.ConnectionInitEvent;
import fr.astfaster.sentinel.proxy.event.impl.ConnectionInitEventImpl;
import fr.astfaster.sentinel.proxy.network.server.pipeline.FrameDecoder;
import fr.astfaster.sentinel.proxy.network.server.pipeline.FrameEncoder;
import fr.astfaster.sentinel.proxy.network.server.pipeline.PacketCodec;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(@NotNull Channel ch) {
        final Config config = Sentinel.instance().config();
        final ChannelPipeline pipeline = ch.pipeline();

        final ConnectionInitEvent event = Sentinel.instance().eventBus().publish(new ConnectionInitEventImpl(ch.remoteAddress())).join();

        if (!event.result().get()) {
            ch.close();
            return;
        }

        pipeline.addLast("frame-decoder", new FrameDecoder());
        pipeline.addLast("timeout", new ReadTimeoutHandler(config.readTimeout(), TimeUnit.MILLISECONDS));
        pipeline.addLast("frame-encoder", new FrameEncoder());
        pipeline.addLast("flow-handler", new FlowControlHandler());
        pipeline.addLast("packet-codec", new PacketCodec());
        pipeline.addLast("packet-handler", new PlayerConnectionImpl(ch));

        if (config.proxyProtocol()) {
            pipeline.addFirst(new HAProxyMessageDecoder());
        }
    }

}
