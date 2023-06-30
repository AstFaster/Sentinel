package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.api.network.protocol.ProxyPing;

public interface ProxyPingEvent extends Event {

    PlayerConnection connection();

    ProxyPing ping();

}
