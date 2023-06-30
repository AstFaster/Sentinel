package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.player.SentinelPlayer;

public interface DisconnectEvent extends Event {

    SentinelPlayer player();

}
