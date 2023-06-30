package fr.astfaster.sentinel.api.network.protocol;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public interface ProxyPing {

    Version version();

    Players players();

    Component description();

    ProxyPing description(Component description);

    Favicon favicon();

    boolean enforceSecureChat();

    ProxyPing enforceSecureChat(boolean secureChat);

    boolean previewsChat();

    ProxyPing previewsChat(boolean previewsChat);

    interface Nested<T> {

        T end();

    }

    interface Version extends Nested<ProxyPing> {

        String name();

        Version name(String name);

        int protocol();

        Version protocol(int protocol);

    }

    interface Players extends Nested<ProxyPing> {

        int max();

        Players max(int max);

        int online();

        Players online(int online);

        Sample[] sample();

        Sample addSample();

    }

    interface Sample extends Nested<Players> {

        String name();

        Sample name(String name);

        UUID id();

        Sample id(UUID id);

    }

    interface Favicon extends Nested<ProxyPing> {

        String base64();

        Favicon base64(String base64);

        Favicon image(@NotNull BufferedImage image);

        Favicon path(@NotNull Path path) throws IOException;

    }

}
