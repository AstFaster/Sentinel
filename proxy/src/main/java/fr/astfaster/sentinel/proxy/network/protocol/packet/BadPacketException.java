package fr.astfaster.sentinel.proxy.network.protocol.packet;

import fr.astfaster.sentinel.api.util.SentinelException;

public class BadPacketException extends SentinelException {

    public BadPacketException(String message) {
        super(message);
    }

}
