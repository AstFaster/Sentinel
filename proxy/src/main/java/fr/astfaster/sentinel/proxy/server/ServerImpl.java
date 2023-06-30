package fr.astfaster.sentinel.proxy.server;

import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.server.Server;
import fr.astfaster.sentinel.api.util.BuilderException;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerImpl implements Server {

    private final String name;
    private final SocketAddress address;
    private final Map<UUID, SentinelPlayer> players = new ConcurrentHashMap<>();

    public ServerImpl(String name, SocketAddress address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public SocketAddress address() {
        return this.address;
    }

    @Override
    public boolean containsPlayer(UUID playerId) {
        return this.players.containsKey(playerId);
    }

    @Override
    public Collection<SentinelPlayer> players() {
        return this.players.values();
    }

    public void addPlayer(SentinelPlayer player) {
        this.players.put(player.uuid(), player);
    }

    public void removePlayer(UUID playerId) {
        this.players.remove(playerId);
    }

    static class Builder implements Server.Builder {

        private String name;
        private SocketAddress address;

        @Override
        public Server.Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @Override
        public Server.Builder address(@NotNull SocketAddress address) {
            this.address = address;
            return this;
        }

        ServerImpl build() {
            if (this.name == null) {
                throw new BuilderException("Server", "name");
            }

            if (this.address == null) {
                throw new BuilderException("Server", "address");
            }

            return new ServerImpl(this.name, this.address);
        }

    }

}
