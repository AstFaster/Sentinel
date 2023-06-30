package fr.astfaster.sentinel.api.network.connection;

import net.kyori.adventure.text.Component;

import java.net.SocketAddress;

public interface Connection {

    boolean active();

    SocketAddress remoteAddress();

    void disconnect(Component reason);

    default void disconnect() {
        this.disconnect(null);
    }

}
