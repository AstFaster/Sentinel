package fr.astfaster.sentinel.proxy.network.protocol.handler;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.player.GameProfile;
import fr.astfaster.sentinel.api.player.SentinelPlayer;
import fr.astfaster.sentinel.api.server.Server;
import fr.astfaster.sentinel.proxy.SentinelServer;
import fr.astfaster.sentinel.proxy.event.impl.LoginEventImpl;
import fr.astfaster.sentinel.proxy.event.impl.PostLoginEventImpl;
import fr.astfaster.sentinel.proxy.event.impl.PreLoginEventImpl;
import fr.astfaster.sentinel.proxy.network.connection.PlayerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.connection.ServerConnectionImpl;
import fr.astfaster.sentinel.proxy.network.protocol.ProtocolState;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginStart;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginSuccess;
import fr.astfaster.sentinel.proxy.network.server.BackendServerInitializer;
import fr.astfaster.sentinel.proxy.network.server.NettyServer;
import fr.astfaster.sentinel.proxy.network.server.Transport;
import fr.astfaster.sentinel.proxy.player.GameProfileImpl;
import fr.astfaster.sentinel.proxy.player.SentinelPlayerImpl;
import fr.astfaster.sentinel.proxy.server.ServerImpl;
import fr.astfaster.sentinel.proxy.util.CharactersChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static fr.astfaster.sentinel.proxy.network.ConnectionManager.WRITE_MARK;

public class LoginHandler implements ConnectionHandler {

    private static final Logger LOGGER = LogManager.getLogger(LoginHandler.class);

    private SentinelPlayerImpl player;
    private final PlayerConnectionImpl connection;

    public LoginHandler(PlayerConnectionImpl connection) {
        this.connection = connection;
    }

    @Override
    public void disconnected(Channel channel) {
        if (Sentinel.instance().config().logs().logDisconnections()) {
            LOGGER.info("{} has disconnected", this);
        }
    }

    @Override
    public void handle(Packet packet) {
        if (packet instanceof final LoginStart login) {
            this.loginStart(login);
        }
    }

    private void loginStart(LoginStart login) {
        final String name = login.username();

        if (!CharactersChecker.isNameValid(name)) {
            this.connection.disconnect(Component.text("Username contains invalid characters!").color(NamedTextColor.RED));
            return;
        }

        final int slots = Sentinel.instance().config().slots();

        if (slots != -1 && Sentinel.instance().counter() >= slots) {
            this.connection.disconnect(Component.text("The proxy is currently full!").color(NamedTextColor.RED));
            return;
        }

        Sentinel.instance().eventBus().publish(new PreLoginEventImpl(this.connection)).thenAccept(event -> {
            if (!this.connection.active()) {
                return;
            }

           if (!event.result().get()) {
               final Component reason = event.cancelReason();

               this.connection.disconnect(reason != null ? reason : Component.text("Disconnected while pre-logging in").color(NamedTextColor.RED));
               return;
           }

            // TODO Check encryption

            // TODO Encryption and auth system
            final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            final GameProfile gameProfile = new GameProfileImpl(uuid, name, new ArrayList<>());

            this.player = new SentinelPlayerImpl(gameProfile, this.connection);
            this.connection.player(this.player);

            if (!SentinelServer.instance().registerPlayer(this.player)) {
                this.connection.disconnect(Component.text("Already connected to the proxy!").color(NamedTextColor.RED));
                return;
            }

            this.loginSuccess();
        });
    }

    private void loginSuccess() {
        Sentinel.instance().eventBus().publish(new LoginEventImpl(this.player)).thenAccept(event -> {
            if (!this.connection.active()) {
                return;
            }

            if (!event.result().get()) {
                final Component reason = event.cancelReason();

                this.connection.disconnect(reason != null ? reason : Component.text("Disconnected while logging in").color(NamedTextColor.RED));
                return;
            }

            Sentinel.instance().eventBus().publish(new PostLoginEventImpl(this.player)).join();

            this.connection.sendPacket(new LoginSuccess(this.player.profile()));
            this.connection.protocolState(ProtocolState.PLAY);
            this.connection.handler(new UpstreamHandler(this.connection));

            this.connect();
        });
    }

    private Supplier<ServerImpl> server = new Supplier<>() {

        private int current = 0;

        @Override
        public ServerImpl get() {
            if (this.current == 1) {
                this.current = 0;
                return (ServerImpl) Sentinel.instance().serversRegistry().server("lobby2");
            }

            this.current++;

            return (ServerImpl) Sentinel.instance().serversRegistry().server("lobby");
        }
    };

    private void connect() {
        final ServerImpl server = this.server.get();
        final Transport transport = Transport.best();
        final Bootstrap bootstrap = new Bootstrap()
                .channel(transport.channel())
                .group(this.connection.channel().eventLoop())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 0x18)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WRITE_MARK)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .remoteAddress(server.address())
                .localAddress(SentinelServer.instance().connectionManager().server().address().getHostString(), 0)
                .handler(new BackendServerInitializer(this.player, server));

        if (transport == Transport.EPOLL) {
            bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, true);
        }

        bootstrap.connect().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                this.connection.channel().eventLoop().schedule(this::connect, 10, TimeUnit.SECONDS);
            } else {
                this.player.disconnect(Component.text(future.cause().getMessage()).color(NamedTextColor.RED));

                future.channel().close();
            }
        });
    }

    @Override
    public String toString() {
        return "[Initial Connection <-> " + this.connection.remoteAddress().toString() + "]";
    }

}
