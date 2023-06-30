package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.impl.ConnectionInitEvent;

import java.net.SocketAddress;

public class ConnectionInitEventImpl implements ConnectionInitEvent {

    private BinaryResult result = BinaryResult.allowed();

    private final SocketAddress remoteAddress;

    public ConnectionInitEventImpl(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public BinaryResult result() {
        return this.result;
    }

    @Override
    public void result(BinaryResult result) {
        this.result = result;
    }

    @Override
    public SocketAddress remoteAddress() {
        return this.remoteAddress;
    }

}
