package fr.astfaster.sentinel.api.player;

import fr.astfaster.sentinel.api.command.CommandSender;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import fr.astfaster.sentinel.api.network.connection.ServerConnection;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface SentinelPlayer extends CommandSender {

    UUID uuid();

    String name();

    GameProfile profile();

    PlayerConnection connection();

    ServerConnection serverConnection();

    void disconnect(Component reason);

    default void disconnect() {
        this.disconnect(null);
    }

}
