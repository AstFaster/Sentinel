package fr.astfaster.sentinel.proxy.config;

import fr.astfaster.sentinel.api.config.Config;
import fr.astfaster.sentinel.api.config.Logs;
import fr.astfaster.sentinel.api.config.ServerBindings;
import fr.astfaster.sentinel.proxy.util.YamlLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigImpl implements Config {

    public static final Path CONFIG_FILE = Paths.get("sentinel.yml");

    private ServerBindingsImpl serverBindings;
    private LogsImpl logs;
    private int readTimeout;
    private boolean proxyProtocol;
    private boolean ipForwarding;
    private boolean onlineMode;
    private int slots;

    public ConfigImpl(ServerBindingsImpl serverBindings, LogsImpl logs, int readTimeout, boolean proxyProtocol, boolean ipForwarding, boolean onlineMode, int slots) {
        this.serverBindings = serverBindings;
        this.logs = logs;
        this.readTimeout = readTimeout;
        this.proxyProtocol = proxyProtocol;
        this.ipForwarding = ipForwarding;
        this.onlineMode = onlineMode;
        this.slots = slots;
    }

    public ConfigImpl() {}

    public static ConfigImpl load() {
        if (Files.exists(CONFIG_FILE)) {
            return YamlLoader.load(CONFIG_FILE, ConfigImpl.class);
        } else {
            final ConfigImpl config = new ConfigImpl(new ServerBindingsImpl(), new LogsImpl(), 30 * 1000, false, true, true, -1);

            YamlLoader.save(CONFIG_FILE, config);

            System.err.println("Please fill configuration file before starting the proxy!");
            System.exit(0);

            return config;
        }
    }

    @Override
    public ServerBindings serverBindings() {
        return this.serverBindings;
    }

    @Override
    public Logs logs() {
        return this.logs;
    }

    @Override
    public int readTimeout() {
        return this.readTimeout;
    }

    @Override
    public boolean proxyProtocol() {
        return this.proxyProtocol;
    }

    @Override
    public boolean ipForwarding() {
        return this.ipForwarding;
    }

    @Override
    public boolean onlineMode() {
        return onlineMode;
    }

    @Override
    public int slots() {
        return this.slots;
    }

}
