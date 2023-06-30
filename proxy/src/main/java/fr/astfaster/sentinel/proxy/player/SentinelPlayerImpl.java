package fr.astfaster.sentinel.proxy.player;

import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.api.network.connection.ServerConnection;
import fr.astfaster.sentinel.api.player.GameProfile;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class SentinelPlayerImpl implements SentinelPlayer {

    private final GameProfile profile;
    private final PlayerConnectionImpl connection;
    private ServerConnectionImpl serverConnection;

    public SentinelPlayerImpl(GameProfile profile, PlayerConnectionImpl connection) {
        this.profile = profile;
        this.connection = connection;
    }

    @Override
    public UUID uuid() {
        return this.profile.id();
    }

    @Override
    public String name() {
        return this.profile.name();
    }

    @Override
    public GameProfile profile() {
        return this.profile;
    }

    @Override
    public PlayerConnection connection() {
        return this.connection;
    }

    @Override
    public ServerConnection serverConnection() {
        return this.serverConnection;
    }

    public void serverConnection(ServerConnectionImpl serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void disconnect(Component reason) {
        this.connection.disconnect(reason);
    }

}
