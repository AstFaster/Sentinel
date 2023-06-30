package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.HandshakeInfoImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Handshake;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class HandshakeHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(HandshakeHandler.class);

    private final PlayerConnectionImpl connection;

    public HandshakeHandler(PlayerConnectionImpl connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Packet packet) {
        final Handshake handshake = (Handshake) packet;
        final ProtocolVersion protocolVersion = ProtocolVersion.from(handshake.protocol());

        if (protocolVersion == null) {
            LOGGER.warn("{} received invalid protocol {}", this, handshake.protocol());
            this.connection.disconnect();
            return;
        }

        final String host = this.cleanHost(handshake.address());

        this.connection.virtualHost(() -> InetSocketAddress.createUnresolved(host, handshake.port()));
        this.connection.protocolVersion(protocolVersion);
        this.connection.handshake(new HandshakeInfoImpl(handshake.protocol(), host, handshake.port(), this.hostExtraData(host)));

        switch (handshake.nextState()) {
            case ProtocolState.STATUS_ID -> {
                this.connection.protocolState(ProtocolState.STATUS);
                this.connection.handler(new StatusHandler(this.connection));
            }
            case ProtocolState.LOGIN_ID -> {
                if (Sentinel.instance().config().logs().logConnections()) {
                    LOGGER.info("{} has connected", this);
                }

                this.connection.protocolState(ProtocolState.LOGIN);
                this.connection.handler(new LoginHandler(this.connection));
            }
            default -> {
                LOGGER.warn("{} received invalid next state {}", this, handshake.nextState());
                this.connection.disconnect();
            }
        }
    }

    private String cleanHost(String host) {
        // Remove everything any zero bytes. For example FML handshake token or BungeeCord's IP Forwarding
        if (host.contains("\0")) {
            host = host.substring(0, host.indexOf("\0"));
        }

        // SRV records might append a dot at the end of the address
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    private String hostExtraData(String host) {
        return host.contains("\0") ? host.substring(host.indexOf("\0")) : null;
    }

    @Override
    public String toString() {
        return "[Initial Connection <-> " + this.connection.remoteAddress().toString() + "]";
    }

}
