package fr.astfaster.sentinel.api;

import fr.astfaster.sentinel.api.command.CommandsRegistry;
import fr.astfaster.sentinel.api.config.Config;
import fr.astfaster.sentinel.api.event.EventBus;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.provider.Providable;
import fr.astfaster.sentinel.api.provider.Provider;
import fr.astfaster.sentinel.api.server.ServersRegistry;

import java.util.Collection;
import java.util.UUID;

public interface Sentinel extends Providable {

    static Sentinel instance() {
        return Provider.provide(Sentinel.class);
    }

    Config config();

    EventBus eventBus();

    CommandsRegistry commandsRegistry();

    ServersRegistry serversRegistry();

    SentinelPlayer player(UUID uuid);

    SentinelPlayer player(String name);

    Collection<SentinelPlayer> matchPlayer(String partialName);

    Collection<SentinelPlayer> players();

    default int counter() {
        return this.players().size();
    }

}
