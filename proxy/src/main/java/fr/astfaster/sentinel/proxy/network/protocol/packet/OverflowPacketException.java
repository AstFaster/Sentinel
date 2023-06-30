package fr.astfaster.sentinel.proxy.network.protocol.packet;

import fr.astfaster.sentinel.api.util.SentinelException;

public class OverflowPacketException extends SentinelException {

    public OverflowPacketException(String message) {
        super(message);
    }

}
