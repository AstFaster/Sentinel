package fr.astfaster.sentinel.proxy.event.impl;

import fr.astfaster.sentinel.api.event.impl.ServerUnregisteredEvent;
import fr.astfaster.sentinel.api.server.Server;

public record ServerUnregisteredEventImpl(Server server) implements ServerUnregisteredEvent {}
