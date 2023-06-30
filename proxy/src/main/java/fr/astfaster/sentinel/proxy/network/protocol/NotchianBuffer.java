package fr.astfaster.sentinel.proxy.network.protocol;

import com.google.common.base.Charsets;
import fr.astfaster.sentinel.proxy.network.protocol.packet.BadPacketException;
import fr.astfaster.sentinel.proxy.network.protocol.packet.OverflowPacketException;
import io.netty.buffer.*;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

public class NotchianBuffer extends ByteBuf {

    private final ByteBuf buf;

    private NotchianBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    public static NotchianBuffer of(ByteBuf buf) {
        return new NotchianBuffer(buf);
    }

    public int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = this.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new BadPacketException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public void writeVarInt(int value) {
        do {
            byte temp = (byte)(value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            this.buf.writeByte(temp);
        } while (value != 0);
    }

    public String readString() {
        return this.readString(Short.MAX_VALUE);
    }

    public String readString(int maxLength) {
        final int length = this.readVarInt();

        if (length > maxLength * 3) {
            throw new OverflowPacketException("Cannot receive string longer than " + maxLength * 3 + " (got " + length + " bytes)" );
        }

        final String string = this.toString(this.readerIndex(), length, Charsets.UTF_8);

        this.readerIndex(this.readerIndex() + length);

        if (string.length() > maxLength) {
            throw new OverflowPacketException( "Cannot receive string longer than " + maxLength + " (got " + string.length() + " characters)" );
        }
        return string;
    }

    public void writeString(CharSequence str) {
        int size = ByteBufUtil.utf8Bytes(str);
        writeVarInt(size);
        this.writeCharSequence(str, StandardCharsets.UTF_8);
    }

    public byte[] readBytesArray() {
        int length = readVarInt();
        byte[] array = new byte[length];
        buf.readBytes(array);
        return array;
    }

    public void writeBytesArray(byte[] array) {
        writeVarInt(array.length);
        buf.writeBytes(array);
    }

    public int[] readIntArray() {
        int len = readVarInt();
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = readVarInt();
        }
        return array;
    }

    public UUID readUUID() {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public void writeUUID(UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public String[] readStringsArray() {
        int length = readVarInt();
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readString();
        }
        return ret;
    }

    public void writeStringsArray(String[] stringArray) {
        writeVarInt(stringArray.length);
        for (String str : stringArray) {
            writeString(str);
        }
    }

    public void writeVarIntArray(int[] array) {
        writeVarInt(array.length);
        for (int i : array) {
            writeVarInt(i);
        }
    }

    public void writeLongArray(long[] array) {
        writeVarInt(array.length);
        for (long i : array) {
            writeLong(i);
        }
    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass) {
        E[] enums = oclass.getEnumConstants();
        BitSet bits = new BitSet(enums.length);

        for (int i = 0; i < enums.length; ++i) {
            bits.set(i, enumset.contains(enums[i]));
        }

        writeFixedBitSet(bits, enums.length, buf);
    }

    public void writeEnum(Enum<?> value) {
        this.writeVarInt(value.ordinal());
    }

    private static void writeFixedBitSet(BitSet bits, int size, ByteBuf buf) {
        if (bits.length() > size) {
            throw new StackOverflowError("BitSet too large (expected " + size + " got " + bits.size() + ")");
        }
        buf.writeBytes(Arrays.copyOf(bits.toByteArray(), (size + 8) >> 3));
    }

    public CompoundBinaryTag readTag(BinaryTagIO.Reader reader) {
        try {
            return reader.read((DataInput) new ByteBufInputStream(this));
        } catch (IOException e) {
            throw new DecoderException("Couldn't read NBT tag!", e);
        }
    }

    public CompoundBinaryTag readTag() {
        return this.readTag(BinaryTagIO.reader());
    }

    public void writeTag(CompoundBinaryTag tag) {
        try {
            BinaryTagIO.writer().write(tag, (DataOutput) new ByteBufOutputStream(this));
        } catch (IOException e) {
            throw new EncoderException("Couldn't write NBT tag!", e);
        }
    }

    @Override
    public int capacity() {
        return this.buf.capacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return this.buf.capacity(newCapacity);
    }

    @Override
    public int maxCapacity() {
        return this.buf.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.buf.alloc();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ByteOrder order() {
        return this.buf.order();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ByteBuf order(ByteOrder endianness) {
        return this.buf.order(endianness);
    }

    @Override
    public ByteBuf unwrap() {
        return this.buf.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.buf.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.buf.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.buf.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.buf.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int readerIndex) {
        return this.buf.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex() {
        return this.buf.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int writerIndex) {
        return this.buf.writerIndex(writerIndex);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.buf.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return this.buf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.buf.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.buf.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.buf.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.buf.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.buf.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.buf.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return this.buf.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.buf.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.buf.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.buf.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.buf.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.buf.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.buf.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        return this.buf.ensureWritable(minWritableBytes);
    }

    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return this.buf.ensureWritable(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.buf.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.buf.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.buf.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.buf.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return this.buf.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.buf.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return this.buf.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return this.buf.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return this.buf.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.buf.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return this.buf.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return this.buf.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return this.buf.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.buf.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return this.buf.getUnsignedInt(index);
    }

    @Override
    public long getLong(int index) {
        return this.buf.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return this.buf.getLong(index);
    }

    @Override
    public char getChar(int index) {
        return this.buf.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.buf.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.buf.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return this.buf.getBytes(index, dst, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return this.buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return this.buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return this.buf.getBytes(index, out, position, length);
    }

    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.buf.getCharSequence(index, length, charset);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return this.buf.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return this.buf.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return this.buf.setShort(index, value);
    }

    @Override
    public ByteBuf setShortLE(int index, int value) {
        return this.buf.setShortLE(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return this.buf.setMedium(index, value);
    }

    @Override
    public ByteBuf setMediumLE(int index, int value) {
        return this.buf.setMediumLE(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return this.buf.setInt(index, value);
    }

    @Override
    public ByteBuf setIntLE(int index, int value) {
        return this.buf.setIntLE(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return this.buf.setLong(index, value);
    }

    @Override
    public ByteBuf setLongLE(int index, long value) {
        return this.buf.setLongLE(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return this.buf.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return this.buf.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return this.buf.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return this.buf.setBytes(index, src, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return this.buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return this.buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return this.buf.setBytes(index, in, position, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return this.buf.setZero(length, index);
    }

    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.buf.setCharSequence(index, sequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.buf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public short readShortLE() {
        return this.buf.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.buf.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.buf.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.buf.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.buf.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.buf.readMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.buf.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.buf.readInt();
    }

    @Override
    public int readIntLE() {
        return this.buf.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.buf.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.buf.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.buf.readLong();
    }

    @Override
    public long readLongLE() {
        return this.buf.readLongLE();
    }

    @Override
    public char readChar() {
        return this.buf.readChar();
    }

    @Override
    public float readFloat() {
        return this.buf.readFloat();
    }

    @Override
    public double readDouble() {
        return this.buf.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.buf.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.buf.readSlice(length);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return this.buf.readRetainedSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return this.buf.readBytes(dst, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return this.buf.readBytes(out, length);
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return this.buf.readBytes(out, length);
    }

    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.buf.readCharSequence(length, charset);
    }

    @Override
    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return this.buf.readBytes(out, position, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return this.buf.skipBytes(length);
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return this.buf.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value) {
        return this.buf.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value) {
        return this.buf.writeShort(value);
    }

    @Override
    public ByteBuf writeShortLE(int value) {
        return this.buf.writeShortLE(value);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return this.buf.writeMedium(value);
    }

    @Override
    public ByteBuf writeMediumLE(int value) {
        return this.buf.writeMediumLE(value);
    }

    @Override
    public ByteBuf writeInt(int value) {
        return this.buf.writeInt(value);
    }

    @Override
    public ByteBuf writeIntLE(int value) {
        return this.buf.writeIntLE(value);
    }

    @Override
    public ByteBuf writeLong(long value) {
        return this.buf.writeLong(value);
    }

    @Override
    public ByteBuf writeLongLE(long value) {
        return this.buf.writeLongLE(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return this.buf.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return this.buf.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return this.buf.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return this.buf.writeBytes(src, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        return this.buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return this.buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return this.buf.writeBytes(in, position, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return this.buf.writeZero(length);
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.buf.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return this.buf.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.buf.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.buf.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.buf.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteProcessor processor) {
        return this.buf.forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        return this.buf.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor processor) {
        return this.buf.forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return this.buf.forEachByteDesc(index, length, processor);
    }

    @Override
    public ByteBuf copy() {
        return this.buf.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.buf.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.buf.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.buf.retainedSlice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.buf.slice(index, length);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return this.buf.retainedSlice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.buf.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.buf.retainedSlice();
    }

    @Override
    public int nioBufferCount() {
        return this.buf.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.buf.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.buf.nioBuffer();
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.buf.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.buf.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.buf.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.buf.hasArray();
    }

    @Override
    public byte[] array() {
        return this.buf.array();
    }

    @Override
    public int arrayOffset() {
        return this.buf.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.buf.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.buf.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.buf.toString(charset);
    }

    @NotNull
    @Override
    public String toString(int index, int length, Charset charset) {
        return this.buf.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.buf.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.buf.equals(obj);
    }

    @Override
    public int compareTo(ByteBuf buffer) {
        return this.buf.compareTo(buffer);
    }

    @Override
    public String toString() {
        return this.buf.toString();
    }

    @Override
    public ByteBuf retain(int increment) {
        return this.buf.retain(increment);
    }

    @Override
    public int refCnt() {
        return this.buf.refCnt();
    }

    @Override
    public ByteBuf retain() {
        return this.buf.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.buf.touch();
    }

    @Override
    public ByteBuf touch(Object hint) {
        return this.buf.touch(hint);
    }

    @Override
    public boolean release() {
        return this.buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.buf.release(decrement);
    }

}
