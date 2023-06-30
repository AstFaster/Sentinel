package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.impl.PostLoginEvent;
import fr.astfaster.sentinel.api.player.SentinelPlayer;

public record PostLoginEventImpl(SentinelPlayer player) implements PostLoginEvent {}
