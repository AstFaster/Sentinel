package fr.astfaster.sentinel.proxy.network.protocol.packet;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.util.Cast;

public interface Packet extends Cast<Packet> {

    void read(ProtocolVersion version, NotchianBuffer buffer);

    void write(ProtocolVersion version, NotchianBuffer buffer);

    default int maxLength(ProtocolVersion version) {
        return -1;
    }

    default int minLength(ProtocolVersion version) {
        return 0;
    }

}
