package fr.astfaster.sentinel.proxy.network.server.pipeline;

import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolBound;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.JoinGame;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.Respawn;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class PacketCodec extends MessageToMessageCodec<ByteBuf, Packet> {

    private ProtocolVersion protocolVersion;
    private ProtocolState protocolState;

    private boolean reversed = false;

    public PacketCodec() {
        this.protocolVersion = ProtocolVersion.UNKNOWN;
        this.protocolState = ProtocolState.HANDSHAKE;
    }

    public PacketCodec(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.protocolState = ProtocolState.HANDSHAKE;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {
        final NotchianBuffer buffer = NotchianBuffer.of(ctx.alloc().buffer());
        final int packetId = this.protocolState.packetRegistry(!this.reversed ? ProtocolBound.CLIENT : ProtocolBound.SERVER, this.protocolVersion).id(packet.getClass());

        buffer.writeVarInt(packetId);
        packet.write(this.protocolVersion, buffer);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        if (!buf.isReadable() || !ctx.channel().isActive()) {
            return;
        }

        final NotchianBuffer buffer = NotchianBuffer.of(buf);
        final NotchianBuffer copy = NotchianBuffer.of(buffer.copy());
        final int packetId = buffer.readVarInt();
        final Packet packet = this.protocolState.packetRegistry(!this.reversed ? ProtocolBound.SERVER : ProtocolBound.CLIENT, this.protocolVersion).constructPacket(packetId);

        if (packet != null) {
            this.checkLength(buffer, packet);

            packet.read(this.protocolVersion, buffer);
        } else {
            buffer.skipBytes(buffer.readableBytes());
        }

        out.add(new PacketWrapper(packet, packetId, copy));
    }

    private void checkLength(NotchianBuffer buffer, Packet packet) {
        final int minLength = packet.minLength(this.protocolVersion);
        final int maxLength = packet.maxLength(this.protocolVersion);
        final int length = buffer.readableBytes();

        if (maxLength != -1 && length > maxLength) {
            throw new CorruptedFrameException("Received " + packet.getClass().getName() + " packet was too big (expected " + maxLength + " bytes, got " + length + " bytes)");
        }

        if (minLength > length) {
            throw new CorruptedFrameException("Received " + packet.getClass().getName() + " packet was too small (expected " + minLength + " bytes, got " + length + " bytes)");
        }
    }

    public void protocolVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void protocolState(ProtocolState protocolState) {
        this.protocolState = protocolState;
    }

    public PacketCodec reversed() {
        this.reversed = true;
        return this;
    }

}

