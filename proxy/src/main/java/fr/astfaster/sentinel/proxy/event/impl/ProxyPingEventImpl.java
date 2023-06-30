package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.impl.ProxyPingEvent;
import fr.astfaster.sentinel.api.network.connection.Connection;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.proxy.network.protocol.ProxyPingImpl;

public class ProxyPingEventImpl implements ProxyPingEvent {

    private final PlayerConnection connection;
    private final ProxyPingImpl ping;

    public ProxyPingEventImpl(PlayerConnection connection) {
        this.connection = connection;
        this.ping = new ProxyPingImpl(this.connection.protocolVersion().protocol());
    }

    @Override
    public PlayerConnection connection() {
        return this.connection;
    }

    @Override
    public ProxyPingImpl ping() {
        return this.ping;
    }

}
