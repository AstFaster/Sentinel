package fr.astfaster.sentinel.proxy.network.protocol;

import fr.astfaster.sentinel.api.network.protocol.HandshakeInfo;

public record HandshakeInfoImpl(int protocol, String address, int port, String extraData) implements HandshakeInfo {}
