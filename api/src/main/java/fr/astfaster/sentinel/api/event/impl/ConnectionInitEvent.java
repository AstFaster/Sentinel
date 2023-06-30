package fr.astfaster.sentinel.api.event.impl;

import fr.astfaster.sentinel.api.event.BinaryResult;
import fr.astfaster.sentinel.api.event.ResultingEvent;

import java.net.SocketAddress;

public interface ConnectionInitEvent extends ResultingEvent<BinaryResult> {

    SocketAddress remoteAddress();

}
