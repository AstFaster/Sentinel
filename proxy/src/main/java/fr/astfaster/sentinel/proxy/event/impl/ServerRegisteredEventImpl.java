package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.impl.ServerRegisteredEvent;
import fr.astfaster.sentinel.api.server.Server;

public record ServerRegisteredEventImpl(Server server) implements ServerRegisteredEvent {}
