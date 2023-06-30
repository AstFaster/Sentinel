package fr.astfaster.sentinel.api.network.connection;

import fr.astfaster.sentinel.api.network.protocol.HandshakeInfo;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;

import java.net.SocketAddress;
import java.util.function.Supplier;

public interface PlayerConnection extends Connection {

    HandshakeInfo handshake();

    Supplier<SocketAddress> virtualHost();

    ProtocolVersion protocolVersion();

}
