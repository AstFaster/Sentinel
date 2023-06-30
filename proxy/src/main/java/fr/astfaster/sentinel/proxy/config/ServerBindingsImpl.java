package fr.astfaster.sentinel.proxy.config;

import fr.astfaster.sentinel.api.config.ServerBindings;

public class ServerBindingsImpl implements ServerBindings {

    private String hostAddress = "0.0.0.0";
    private int port = 25565;

    public ServerBindingsImpl(String hostAddress, int port) {
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public ServerBindingsImpl() {}

    @Override
    public String hostAddress() {
        return this.hostAddress;
    }

    @Override
    public int port() {
        return this.port;
    }

}