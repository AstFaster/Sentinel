package fr.astfaster.sentinel.api.network.connection;

import fr.astfaster.sentinel.api.server.Server;

public interface ServerConnection extends Connection {

    Server server();

}
