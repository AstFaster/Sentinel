package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.server.Server;

public interface ServerRegisteredEvent extends Event {

    Server server();

}
