package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;

public class LoginStart implements Packet {

    private String username;

    public LoginStart() {}

    public LoginStart(String username) {
        this.username = username;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        this.username = buffer.readString(16);
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeString(this.username);
    }

    public String username() {
        return username;
    }

    @Override
    public int maxLength(ProtocolVersion version) {
        if (version.moreOrEqual(ProtocolVersion.V_1_19)) {
            return -1;
        }
        return 16 * 4 + 1;
    }

}
