package fr.astfaster.sentinel.api.network.protocol;

public interface HandshakeInfo {

    int protocol();

    String address();

    int port();

    String extraData();

}
