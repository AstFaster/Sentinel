package fr.astfaster.sentinel.api.config;

public interface Config {

    ServerBindings serverBindings();

    Logs logs();

    int readTimeout();

    boolean proxyProtocol();

    boolean ipForwarding();

    boolean onlineMode();

    int slots();

}
