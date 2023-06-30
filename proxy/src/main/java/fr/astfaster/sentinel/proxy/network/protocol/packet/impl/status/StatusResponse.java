package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;

public class StatusResponse implements Packet {

    private String json;

    public StatusResponse() {}

    public StatusResponse(String json) {
        this.json = json;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        this.json = buffer.readString();
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeString(this.json);
    }

}
