package fr.astfaster.sentinel.proxy.network.protocol;

import com.google.gson.*;
import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.network.protocol.ProxyPing;
import fr.astfaster.sentinel.proxy.util.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ProxyPingImpl implements ProxyPing {

    private final Version version;
    private final Players players;
    private Component description = Component.text("Sentinel").color(NamedTextColor.RED).toBuilder()
            .append(Component.text(" ┃ ").color(NamedTextColor.DARK_GRAY))
            .append(Component.text("The proxy is listening for connections").color(NamedTextColor.GRAY))
            .build();
    private final Favicon favicon;
    private boolean enforceSecureChat = true;
    private boolean previewsChat = true;

    private final int protocol;

    public ProxyPingImpl(int protocol) {
        this.protocol = protocol;
        this.version = new Version();
        this.players = new Players();
        this.favicon = new Favicon();
    }

    @Override
    public ProxyPing.Version version() {
        return this.version;
    }

    @Override
    public ProxyPing.Players players() {
        return this.players;
    }

    @Override
    public Component description() {
        return this.description;
    }

    @Override
    public ProxyPing description(Component description) {
        this.description = description;
        return this;
    }

    @Override
    public ProxyPing.Favicon favicon() {
        return this.favicon;
    }

    @Override
    public boolean enforceSecureChat() {
        return this.enforceSecureChat;
    }

    @Override
    public ProxyPing enforceSecureChat(boolean secureChat) {
        this.enforceSecureChat = secureChat;
        return this;
    }

    @Override
    public boolean previewsChat() {
        return this.previewsChat;
    }

    @Override
    public ProxyPing previewsChat(boolean previewsChat) {
        this.previewsChat = previewsChat;
        return this;
    }

    public String serialize() {
        return Serializers.PING_SERIALIZER.toJson(this);
    }

    private class Version implements ProxyPing.Version {

        private String name = "Sentinel Proxy";
        private int protocol = ProxyPingImpl.this.protocol;

        @Override
        public ProxyPing end() {
            return ProxyPingImpl.this;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public ProxyPing.Version name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public int protocol() {
            return this.protocol;
        }

        @Override
        public ProxyPing.Version protocol(int protocol) {
            this.protocol = protocol;
            return this;
        }

    }

    private class Players implements ProxyPing.Players {

        private int max = Sentinel.instance().config().slots();
        private int online = Sentinel.instance().counter();

        private Sample[] sample = new Sample[]{};

        @Override
        public ProxyPing end() {
            return ProxyPingImpl.this;
        }

        @Override
        public int max() {
            return this.max;
        }

        @Override
        public ProxyPing.Players max(int max) {
            this.max = max;
            return this;
        }

        @Override
        public int online() {
            return this.online;
        }

        @Override
        public ProxyPing.Players online(int online) {
            this.online = online;
            return this;
        }

        @Override
        public Sample[] sample() {
            return this.sample;
        }

        @Override
        public Sample addSample() {
            final Sample newSample = new Sample();
            final List<Sample> sample = new ArrayList<>(List.of(this.sample));

            sample.add(newSample);

            this.sample = sample.toArray(new Sample[0]);

            return newSample;
        }

    }

    private class Sample implements ProxyPing.Sample {

        private String name;
        private UUID id;

        @Override
        public ProxyPing.Players end() {
            return ProxyPingImpl.this.players;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public ProxyPing.Sample name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public UUID id() {
            return this.id;
        }

        @Override
        public ProxyPing.Sample id(UUID id) {
            this.id = id;
            return this;
        }

    }

    public class Favicon implements ProxyPing.Favicon {

        private String base64 = null;

        @Override
        public ProxyPing end() {
            return ProxyPingImpl.this;
        }

        @Override
        public String base64() {
            return this.base64;
        }

        @Override
        public ProxyPing.Favicon base64(String base64) {
            this.base64 = base64;
            return this;
        }

        @Override
        public ProxyPing.Favicon image(@NotNull BufferedImage image) {
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                throw new IllegalArgumentException("Invalid server icon size (must be 64x64)");
            }

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                ImageIO.write(image, "PNG", outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this.base64("data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        }

        @Override
        public ProxyPing.Favicon path(@NotNull Path path) throws IOException {
            try (final InputStream inputStream = Files.newInputStream(path)) {
                final BufferedImage image = ImageIO.read(inputStream);

                if (image == null) {
                    throw new IOException("Unable to read an image from " + path + "");
                }

                return this.image(image);
            }
        }

        public static class Serializer implements JsonSerializer<Favicon>, JsonDeserializer<Favicon> {

            @Override
            public JsonElement serialize(Favicon src, Type typeOfSrc, JsonSerializationContext ctx) {
                return new JsonPrimitive(src.base64());
            }

            @Override
            public Favicon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return (Favicon) new ProxyPingImpl(-1).favicon().base64(json.getAsString());
            }

        }

    }

    public static class Serializer implements JsonSerializer<ProxyPingImpl> {

        @Override
        public JsonElement serialize(ProxyPingImpl src, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject json = new JsonObject();

            json.add("version", ctx.serialize(src.version));
            json.add("players", ctx.serialize(src.players));
            json.add("description", ctx.serialize(src.description));
            json.addProperty("favicon", src.favicon().base64());
            json.addProperty("enforceSecureChat", src.enforceSecureChat());
            json.addProperty("previewsChat", src.previewsChat());

            return json;
        }

    }
}
