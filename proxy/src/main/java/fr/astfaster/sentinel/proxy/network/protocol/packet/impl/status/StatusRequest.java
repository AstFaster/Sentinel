package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;

public class StatusRequest implements Packet {

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {}

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {}

}
