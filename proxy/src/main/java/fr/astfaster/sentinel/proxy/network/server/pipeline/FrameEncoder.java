package fr.astfaster.sentinel.proxy.network.server.pipeline;

import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class FrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // Varints are never longer than 5 bytes
        final ByteBuf lengthBuf = ctx.alloc().heapBuffer(5);

        NotchianBuffer.of(lengthBuf).writeVarInt(msg.readableBytes());

        out.add(lengthBuf);
        out.add(msg.retain());
    }

}
