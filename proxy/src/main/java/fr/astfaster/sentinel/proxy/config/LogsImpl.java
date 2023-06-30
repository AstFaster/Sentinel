package fr.astfaster.sentinel.proxy.config;

import fr.astfaster.sentinel.api.config.Logs;

public class LogsImpl implements Logs {

    private boolean logPings = true;
    private boolean logConnections = true;
    private boolean logDisconnections = true;
    private boolean logIpForwarding = true;
    private boolean logServersConnections = true;
    private boolean logServersDisconnections = true;

    public LogsImpl() {}

    public LogsImpl(boolean logPings, boolean logConnections, boolean logDisconnections, boolean logIpForwarding, boolean logServersConnections, boolean logServersDisconnections) {
        this.logPings = logPings;
        this.logConnections = logConnections;
        this.logDisconnections = logDisconnections;
        this.logIpForwarding = logIpForwarding;
        this.logServersConnections = logServersConnections;
        this.logServersDisconnections = logServersDisconnections;
    }

    @Override
    public boolean logPings() {
        return this.logPings;
    }

    @Override
    public boolean logConnections() {
        return this.logConnections;
    }

    @Override
    public boolean logDisconnections() {
        return this.logDisconnections;
    }

    @Override
    public boolean logIpForwarding() {
        return this.logIpForwarding;
    }

    @Override
    public boolean logServersConnections() {
        return this.logServersConnections;
    }

    @Override
    public boolean logServersDisconnections() {
        return this.logServersDisconnections;
    }

}
