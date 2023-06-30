package fr.astfaster.sentinel.proxy;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.command.CommandsRegistry;
import fr.astfaster.sentinel.api.config.Config;
import fr.astfaster.sentinel.api.event.EventBus;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.provider.Provider;
import fr.astfaster.sentinel.api.server.ServersRegistry;
import fr.astfaster.sentinel.proxy.command.CommandsRegistryImpl;
import fr.astfaster.sentinel.proxy.config.ConfigImpl;
import fr.astfaster.sentinel.proxy.event.DefaultEventBus;
import fr.astfaster.sentinel.proxy.network.ConnectionManager;
import fr.astfaster.sentinel.proxy.server.ServersRegistryImpl;
import fr.astfaster.sentinel.proxy.terminal.SentinelConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SentinelServer implements Sentinel {

    private final Logger logger;
    private final SentinelConsole console;

    private Config config;

    private final EventBus eventBus;
    private final ServersRegistry serversRegistry;
    private final CommandsRegistry commandsRegistry;

    private final ConnectionManager connectionManager;

    private final Map<UUID, SentinelPlayer> playersById = new ConcurrentHashMap<>();
    private final Map<String, SentinelPlayer> playersByName = new ConcurrentHashMap<>();

    public SentinelServer() {
        Provider.register(Sentinel.class, this);
        Provider.register(SentinelServer.class, this);

        this.logger = LogManager.getLogger(SentinelServer.class);
        this.console = new SentinelConsole();
        this.eventBus = new DefaultEventBus();
        this.serversRegistry = new ServersRegistryImpl();
        this.commandsRegistry = new CommandsRegistryImpl();
        this.connectionManager = new ConnectionManager();
    }

    public void start(SentinelOptions options) {
        this.printHeader();

        this.config = ConfigImpl.load();
        this.console.start();
        this.connectionManager.init();

        this.serversRegistry.buildServer(builder -> builder.name("lobby").address(new InetSocketAddress("localhost", 25565)));
        this.serversRegistry.buildServer(builder -> builder.name("lobby2").address(new InetSocketAddress("localhost", 25566)));

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void printHeader() {
        System.out.println("""
                  /$$$$$$                        /$$     /$$                     /$$
                 /$$__  $$                      | $$    |__/                    | $$
                | $$  \\__/  /$$$$$$  /$$$$$$$  /$$$$$$   /$$ /$$$$$$$   /$$$$$$ | $$
                |  $$$$$$  /$$__  $$| $$__  $$|_  $$_/  | $$| $$__  $$ /$$__  $$| $$
                 \\____  $$| $$$$$$$$| $$  \\ $$  | $$    | $$| $$  \\ $$| $$$$$$$$| $$
                 /$$  \\ $$| $$_____/| $$  | $$  | $$ /$$| $$| $$  | $$| $$_____/| $$
                |  $$$$$$/|  $$$$$$$| $$  | $$  |  $$$$/| $$| $$  | $$|  $$$$$$$| $$
                 \\______/  \\_______/|__/  |__/   \\___/  |__/|__/  |__/ \\_______/|__/""".replaceAll("\\$", "█"));
    }

    public void shutdown() {
        this.connectionManager.shutdown();
        this.console.shutdown();
    }

    public static SentinelServer instance() {
        return Provider.provide(SentinelServer.class);
    }

    @Override
    public Config config() {
        return this.config;
    }

    @Override
    public EventBus eventBus() {
        return this.eventBus;
    }

    @Override
    public ServersRegistry serversRegistry() {
        return this.serversRegistry;
    }

    @Override
    public CommandsRegistry commandsRegistry() {
        return this.commandsRegistry;
    }

    public boolean registerPlayer(SentinelPlayer player) {
        final String name = player.name().toLowerCase();

        if (this.playersByName.putIfAbsent(name, player) != null) {
            return false;
        }

        if (this.playersById.putIfAbsent(player.uuid(), player) != null) {
            this.playersByName.remove(name);
            return false;
        }
        return true;
    }

    public void unregisterPlayer(SentinelPlayer player) {
        this.playersByName.remove(player.name().toLowerCase());
        this.playersById.remove(player.uuid());
    }

    @Override
    public SentinelPlayer player(UUID uuid) {
        return this.playersById.get(uuid);
    }

    @Override
    public SentinelPlayer player(String name) {
        return this.playersByName.get(name.toLowerCase());
    }

    @Override
    public Collection<SentinelPlayer> matchPlayer(String partialName) {
        return this.players().stream().filter(player -> player.name().regionMatches(true, 0, partialName, 0, partialName.length())).collect(Collectors.toList());
    }

    @Override
    public Collection<SentinelPlayer> players() {
        return this.playersById.values();
    }

    public Logger logger() {
        return this.logger;
    }

    public ConnectionManager connectionManager() {
        return this.connectionManager;
    }

}
