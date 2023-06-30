package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.ResultingEvent;
import fr.astfaster.sentinel.api.network.connection.Connection;
import fr.astfaster.sentinel.api.network.connection.PlayerConnection;
import net.kyori.adventure.text.Component;

public interface PreLoginEvent extends ResultingEvent<BinaryResult> {

    boolean encrypting();

    void encrypting(boolean encrypting);

    PlayerConnection connection();

    Component cancelReason();

    void cancelReason(Component reason);

}
