package fr.astfaster.sentinel.proxy.network.protocol.packet;

import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;

public class PacketWrapper {

    private final Packet packet;
    private final int packetId;
    private final NotchianBuffer buffer;

    private boolean released = false;

    public PacketWrapper(Packet packet, int packetId, NotchianBuffer buffer) {
        this.packet = packet;
        this.packetId = packetId;
        this.buffer = buffer;
    }

    public Packet packet() {
        return this.packet;
    }

    public int packetId() {
        return this.packetId;
    }

    public NotchianBuffer buffer() {
        return this.buffer;
    }

    public void release() {
        if (!this.released) {
            this.buffer.release();
        }
    }

    public void released() {
        this.released = true;
    }

}