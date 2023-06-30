package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.impl.PreLoginEvent;
import fr.astfaster.sentinel.api.network.connection.Connection;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import net.kyori.adventure.text.Component;

public class PreLoginEventImpl implements PreLoginEvent {

    private BinaryResult result = BinaryResult.allowed();
    private Component cancelReason;
    private boolean encrypting = Sentinel.instance().config().onlineMode();

    private final PlayerConnection connection;

    public PreLoginEventImpl(PlayerConnection connection) {
        this.connection = connection;
    }

    @Override
    public BinaryResult result() {
        return this.result;
    }

    @Override
    public void result(BinaryResult result) {
        this.result = result;
    }

    @Override
    public boolean encrypting() {
        return this.encrypting;
    }

    @Override
    public void encrypting(boolean encrypting) {
        this.encrypting = encrypting;
    }

    @Override
    public PlayerConnection connection() {
        return this.connection;
    }

    @Override
    public Component cancelReason() {
        return this.cancelReason;
    }

    @Override
    public void cancelReason(Component reason) {
        this.cancelReason = reason;
    }

}
