package fr.astfaster.sentinel.proxy.network.protocol.packet.impl;

import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;

public class Handshake implements Packet {

    private int protocol;
    private String address;
    private int port;
    private int nextState;

    public Handshake() {}

    public Handshake(int protocol, String address, int port, int nextState) {
        this.protocol = protocol;
        this.address = address;
        this.port = port;
        this.nextState = nextState;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        this.protocol = buffer.readVarInt();
        this.address = buffer.readString();
        this.port = buffer.readUnsignedShort();
        this.nextState = buffer.readVarInt();
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeVarInt(this.protocol);
        buffer.writeString(this.address);
        buffer.writeShort(this.port);
        buffer.writeVarInt(this.nextState);
    }

    public int protocol() {
        return protocol;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }

    public int nextState() {
        return nextState;
    }

}
