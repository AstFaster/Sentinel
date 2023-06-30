package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;

public class PingResponse implements Packet {

    private long id;

    public PingResponse() {}

    public PingResponse(long id) {
        this.id = id;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        this.id = buffer.readLong();
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeLong(this.id);
    }

}
