package fr.astfaster.sentinel.api.config;

public interface Logs {

    boolean logPings();

    boolean logConnections();

    boolean logDisconnections();

    boolean logIpForwarding();

    boolean logServersConnections();

    boolean logServersDisconnections();

}
