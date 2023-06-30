package fr.astfaster.sentinel.proxy.network.server;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Transport {

    EPOLL(0, type -> new EpollEventLoopGroup(0, new CustomThreadFactory("Netty Epoll " + type.getName() + " #%d")), EpollServerSocketChannel.class, EpollSocketChannel.class, Epoll::isAvailable),
    NIO(1, type -> new NioEventLoopGroup(0, new CustomThreadFactory("Netty NIO " + type.getName() + " #%d")), NioServerSocketChannel.class, NioSocketChannel.class, () -> true);

    private final int priority;

    private final Function<Type, EventLoopGroup> eventLoop;
    private final Class<? extends ServerChannel> serverChannel;
    private final Class<? extends Channel> channel;
    private final Supplier<Boolean> availability;

    Transport(int priority, Function<Type, EventLoopGroup> eventLoop, Class<? extends ServerChannel> serverChannel, Class<? extends Channel> channel, Supplier<Boolean> availability) {
        this.priority = priority;
        this.eventLoop = eventLoop;
        this.serverChannel = serverChannel;
        this.channel = channel;
        this.availability = availability;
    }

    public int priority() {
        return this.priority;
    }

    public EventLoopGroup newEventLoop(Type type) {
        return this.eventLoop.apply(type);
    }

    public Class<? extends ServerChannel> serverChannel() {
        return this.serverChannel;
    }

    public Class<? extends Channel> channel() {
        return this.channel;
    }

    public boolean isAvailable() {
        return this.availability.get();
    }

    public static Transport best() {
        Transport result = null;
        for (Transport transport : values()) {
            if (transport.isAvailable() && (result == null || transport.priority() < result.priority())) {
                result = transport;
            }
        }
        return result;
    }

    public enum Type {

        BOSS("Boss"),
        WORKER("Worker");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    private static class CustomThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger();

        private final String threadNameFormat;

        public CustomThreadFactory(String threadNameFormat) {
            this.threadNameFormat = threadNameFormat;
        }

        @Override
        public Thread newThread(@NotNull Runnable task) {
            return new FastThreadLocalThread(task, String.format(this.threadNameFormat, this.threadNumber.getAndIncrement()));
        }

    }

}
