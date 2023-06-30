package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.impl.DisconnectEvent;
import fr.astfaster.sentinel.api.player.SentinelPlayer;

public record DisconnectEventImpl(SentinelPlayer player) implements DisconnectEvent {}
