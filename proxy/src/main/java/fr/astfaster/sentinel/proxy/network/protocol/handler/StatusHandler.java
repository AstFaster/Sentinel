package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.proxy.event.impl.ProxyPingEventImpl;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProxyPingImpl;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.PingRequest;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.PingResponse;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.StatusRequest;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.StatusResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(StatusHandler.class);

    private final PlayerConnectionImpl connection;

    public StatusHandler(PlayerConnectionImpl connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Packet packet) {
        if (packet instanceof final PingRequest ping) {
            this.connection.sendPacket(new PingResponse(ping.id()));
        } else if (packet instanceof StatusRequest) {
            Sentinel.instance().eventBus().publish(new ProxyPingEventImpl(this.connection)).thenAcceptAsync(event -> {
                final ProxyPingImpl ping = event.ping();
                final String json = ping.serialize();

                this.connection.sendPacket(new StatusResponse(json));

                if (Sentinel.instance().config().logs().logPings()) {
                    LOGGER.info("{} has pinged", this);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "[Initial Connection <-> " + this.connection.remoteAddress().toString() + "]";
    }

}
