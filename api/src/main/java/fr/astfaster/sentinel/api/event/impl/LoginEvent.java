package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.ResultingEvent;
import fr.astfaster.sentinel.api.network.connection.Connection;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import net.kyori.adventure.text.Component;

public interface LoginEvent extends ResultingEvent<BinaryResult> {

    SentinelPlayer player();

    Component cancelReason();

    void cancelReason(Component reason);

}
