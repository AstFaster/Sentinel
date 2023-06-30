package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.PacketWrapper;
import io.netty.channel.Channel;

public interface ConnectionHandler {

    default void connected(Channel channel) {}

    default void disconnected(Channel channel) {}

    default void exception(Throwable cause) {}

    default void handle(PacketWrapper wrapper) {}

    default void handle(Packet packet) {}

    @Override
    String toString();

}
