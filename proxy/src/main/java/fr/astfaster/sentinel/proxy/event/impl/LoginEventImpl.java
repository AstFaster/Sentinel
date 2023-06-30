package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.impl.LoginEvent;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import net.kyori.adventure.text.Component;

public class LoginEventImpl implements LoginEvent {

    private BinaryResult result = BinaryResult.allowed();
    private Component cancelReason;

    private final SentinelPlayer player;

    public LoginEventImpl(SentinelPlayer player) {
        this.player = player;
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
    public SentinelPlayer player() {
        return this.player;
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
