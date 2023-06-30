package fr.astfaster.sentinel.proxy.network.connection;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.network.connection.Connection;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.proxy.network.protocol.handler.ConnectionHandler;
import fr.astfaster.sentinel.proxy.network.protocol.packet.BadPacketException;
import fr.astfaster.sentinel.proxy.network.protocol.packet.OverflowPacketException;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Disconnect;
import fr.astfaster.sentinel.proxy.network.server.pipeline.PacketCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class DefaultConnection extends ChannelInboundHandlerAdapter implements Connection {

    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected final Channel channel;
    protected SocketAddress remoteAddress;

    protected ProtocolState protocolState = ProtocolState.HANDSHAKE;
    protected ConnectionHandler handler;

    public DefaultConnection(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        final Channel channel = ctx.channel();

        channel.config().setAutoRead(true);

        this.remoteAddress = ctx.channel().remoteAddress();
        this.handler.connected(channel);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof final HAProxyMessage proxy) { // HAProxy IP Forwarding
            try {
                if (proxy.sourceAddress() != null) {
                    final InetSocketAddress newAddress = new InetSocketAddress(proxy.sourceAddress(), proxy.sourcePort());

                    if (Sentinel.instance().config().logs().logIpForwarding()) {
                        this.logger.info("Changed remote address via PROXY {} -> {}", this.remoteAddress, newAddress);
                    }

                    this.remoteAddress = newAddress;
                }
            } finally {
                proxy.release();
            }
        } else if (msg instanceof final PacketWrapper wrapper) {
           try {
               if (this.handler != null) {
                   final Packet packet = wrapper.packet();

                   if (packet != null) {
                       this.handler.handle(packet);
                   } else {
                       this.handler.handle(wrapper);
                   }
               }
           } finally {
               wrapper.release();
           }
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        if (this.handler != null) {
            this.handler.disconnected(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isActive()) {
            this.handler.exception(cause);
        }

        if (cause instanceof ReadTimeoutException) {
            this.logger.warn("{} read timed out", this.handler, cause);
        } else if (cause instanceof BadPacketException) {
            this.logger.warn("{} bad packet received", this.handler, cause);
        } else if (cause instanceof OverflowPacketException) {
            this.logger.warn("{} overflow in packet detected", this.handler, cause);
        } else if (cause instanceof IOException) {
            this.logger.warn("{} {}: {}", this.handler, cause.getClass().getSimpleName(), cause.getMessage());
        } else {
            this.logger.error("{} encountered an error", this.handler, cause);
        }

        ctx.close();
    }

    public void sendPacket(PacketWrapper wrapper) {
        if (this.channel.isActive()) {
            wrapper.released();

            this.channel.writeAndFlush(wrapper.buffer());
        }
    }

    public void sendPacket(Packet packet) {
        if (this.channel.isActive()) {
            this.channel.writeAndFlush(packet);
        }
    }

    protected void close(Disconnect packet) {
        if (packet != null && this.channel.isActive()) {
            this.channel.writeAndFlush(packet).addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE);
        } else {
            this.channel.flush();
            this.channel.close();
        }
    }

    public Channel channel() {
        return this.channel;
    }

    @Override
    public boolean active() {
        return this.channel.isActive();
    }

    @Override
    public SocketAddress remoteAddress() {
        return this.remoteAddress;
    }

    public void protocolState(ProtocolState protocolState) {
        this.protocolState = protocolState;
        this.channel.pipeline().get(PacketCodec.class).protocolState(protocolState);
    }

    public ProtocolState protocolState() {
        return this.protocolState;
    }

    public ConnectionHandler handler() {
        return this.handler;
    }

    public void handler(ConnectionHandler handler) {
        this.handler = handler;
    }

}
