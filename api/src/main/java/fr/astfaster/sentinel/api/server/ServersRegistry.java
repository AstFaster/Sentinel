package fr.astfaster.sentinel.api.server;

import java.util.Map;
import java.util.function.Consumer;

public interface ServersRegistry {

    Server buildServer(Consumer<Server.Builder> builder);

    void removeServer(String name);

    Server server(String name);

    Map<String, Server> servers();

}
