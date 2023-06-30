package fr.astfaster.sentinel.api.server;

import fr.astfaster.sentinel.api.player.SentinelPlayer;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.UUID;

public interface Server {

    String name();

    SocketAddress address();

    boolean containsPlayer(UUID playerId);

    Collection<SentinelPlayer> players();

    interface Builder {

        Builder name(@NotNull String name);

        Builder address(@NotNull SocketAddress address);

    }

}
