package fr.astfaster.sentinel.proxy.server;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.server.Server;
import fr.astfaster.sentinel.api.server.ServersRegistry;
import fr.astfaster.sentinel.api.util.SentinelException;
import fr.astfaster.sentinel.proxy.event.impl.ServerRegisteredEventImpl;
import fr.astfaster.sentinel.proxy.event.impl.ServerUnregisteredEventImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServersRegistryImpl implements ServersRegistry {

    private final Map<String, Server> servers = new ConcurrentHashMap<>();

    @Override
    public Server buildServer(Consumer<Server.Builder> consumer) {
        final ServerImpl.Builder builder = new ServerImpl.Builder();

        consumer.accept(builder);

        final ServerImpl server = builder.build();

        if (this.servers.putIfAbsent(server.name(), server) != null) {
            throw new SentinelException("'" + server.name() + "' server already exists");
        }

        try {
            return server;
        } finally {
            Sentinel.instance().eventBus().publish(new ServerRegisteredEventImpl(server)).join();
        }
    }

    @Override
    public void removeServer(String name) {
        final ServerImpl server = (ServerImpl) this.servers.remove(name);

        if (server != null) {
            // TODO Kick all players

            Sentinel.instance().eventBus().publish(new ServerUnregisteredEventImpl(server)).join();
        }
    }

    @Override
    public Server server(String name) {
        return this.servers.get(name);
    }

    @Override
    public Map<String, Server> servers() {
        return this.servers;
    }

}
