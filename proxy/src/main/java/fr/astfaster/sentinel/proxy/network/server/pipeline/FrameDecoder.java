package fr.astfaster.sentinel.proxy.network.server.pipeline;

import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Dispose of unreadable buffers
        if (!in.isReadable()) {
            return;
        }

        int origReaderIndex = in.readerIndex();

        // We might need to read a few bytes until we get some data
        for (int i = 0; i < 3; i++) {
            if (!in.isReadable()) {
                in.readerIndex(origReaderIndex);
                return;
            }

            byte read = in.readByte();
            if (read >= 0) {
                in.readerIndex(origReaderIndex);
                int packetLength = NotchianBuffer.of(in).readVarInt();

                if (in.readableBytes() >= packetLength) {
                    out.add(in.readRetainedSlice(packetLength));
                } else {
                    in.readerIndex(origReaderIndex);
                }

                return;
            }
        }
        throw new CorruptedFrameException("VarInt too big!");
    }

}
