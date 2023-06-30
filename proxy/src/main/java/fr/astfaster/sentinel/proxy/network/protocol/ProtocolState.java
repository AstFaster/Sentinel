package fr.astfaster.sentinel.proxy.network.protocol;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.api.util.SentinelException;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Disconnect;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.Handshake;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginStart;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login.LoginSuccess;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.JoinGame;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play.Respawn;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.PingRequest;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.PingResponse;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.StatusRequest;
import fr.astfaster.sentinel.proxy.network.protocol.packet.impl.status.StatusResponse;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static fr.astfaster.sentinel.api.network.protocol.ProtocolVersion.*;

public enum ProtocolState {

    HANDSHAKE {{
        this.server.register(Handshake.class, Handshake::new, map(0x00, MINIMAL_VERSION));
    }},

    STATUS {{
        this.server.register(StatusRequest.class, StatusRequest::new, map(0x00, MINIMAL_VERSION));
        this.server.register(PingRequest.class, PingRequest::new, map(0x01, MINIMAL_VERSION));

        this.client.register(StatusResponse.class, StatusResponse::new, map(0x00, MINIMAL_VERSION));
        this.client.register(PingResponse.class, PingResponse::new, map(0x01, MINIMAL_VERSION));
    }},

    LOGIN {{
        this.server.register(LoginStart.class, LoginStart::new, map(0x00, MINIMAL_VERSION));

        this.client.register(Disconnect.class, Disconnect::new, map(0x00, MINIMAL_VERSION));
        this.client.register(LoginSuccess.class, LoginSuccess::new, map(0x02, MINIMAL_VERSION));
    }},

    PLAY {{
        this.client.register(Disconnect.class, Disconnect::new,
                map(0x40, MINIMAL_VERSION),
                map(0x1A, V_1_9),
                map(0x1B, V_1_13),
                map(0x1A, V_1_14),
                map(0x1B, V_1_15),
                map(0x1A, V_1_16),
                map(0x19, V_1_16_2),
                map(0x1A, V_1_17),
                map(0x17, V_1_19),
                map(0x19, V_1_19_1),
                map(0x17, V_1_19_3),
                map(0x1A, V_1_19_4));
        this.client.register(JoinGame.class, JoinGame::new,
                map(0x01, MINIMAL_VERSION),
                map(0x23, V_1_9),
                map(0x25, V_1_13),
                map(0x25, V_1_14),
                map(0x26, V_1_15),
                map(0x25, V_1_16),
                map(0x24, V_1_16_2),
                map(0x26, V_1_17),
                map(0x23, V_1_19),
                map(0x25, V_1_19_1),
                map(0x24, V_1_19_3),
                map(0x28, V_1_19_4));
        this.client.register(Respawn.class, Respawn::new,
                map(0x07, MINIMAL_VERSION),
                map(0x33, V_1_9),
                map(0x34, V_1_12),
                map(0x35, V_1_12_1),
                map(0x38, V_1_13),
                map(0x3A, V_1_14),
                map(0x3B, V_1_15),
                map(0x3A, V_1_16),
                map(0x39, V_1_16_2),
                map(0x3D, V_1_17),
                map(0x3B, V_1_19),
                map(0x3E, V_1_19_1),
                map(0x3D, V_1_19_3),
                map(0x41, V_1_19_4));
    }};

    public static final int STATUS_ID = 1;
    public static final int LOGIN_ID = 2;

    protected final ProtocolRepository client = new ProtocolRepository(ProtocolBound.CLIENT);
    protected final ProtocolRepository server = new ProtocolRepository(ProtocolBound.SERVER);

    public PacketRegistry packetRegistry(ProtocolBound bound, ProtocolVersion version) {
        switch (bound) {
            case CLIENT -> {
                return this.client.packetRegistry(version);
            }
            case SERVER -> {
                return this.server.packetRegistry(version);
            }
        }
        return null;
    }

    public static class ProtocolRepository {

        private final ProtocolBound bound;
        private final Map<ProtocolVersion, PacketRegistry> registries = new EnumMap<>(ProtocolVersion.class);

        public ProtocolRepository(ProtocolBound bound) {
            this.bound = bound;

            for (ProtocolVersion version : ProtocolVersion.values()) {
                if (version.unknown()) {
                    continue;
                }

                this.registries.put(version, new PacketRegistry(version));
            }
        }

        PacketRegistry packetRegistry(ProtocolVersion version) {
            if (version.unknown()) {
                return this.registries.get(MINIMAL_VERSION);
            }
            return this.registries.get(version);
        }

        <T extends Packet> void register(Class<T> packetClass, Supplier<T> packetConstructor, PacketMapping... mappings) {
            for (PacketMapping mapping : mappings) {
                for (ProtocolVersion version : between(mapping.fromVersion(), mapping.toVersion())) {
                    final PacketRegistry registry = this.registries.get(version);

                    registry.register(mapping.packetId(), packetClass, packetConstructor);
                }
            }
        }

    }

    public static class PacketRegistry {

        private final ProtocolVersion version;
        private final Map<Integer, Supplier<? extends Packet>> idToPacket = new HashMap<>(16, 0.5f);
        private final Map<Class<? extends Packet>, Integer> classToId = new HashMap<>(16, 0.5f);

        public PacketRegistry(ProtocolVersion version) {
            this.version = version;
        }

        <T extends Packet> void register(int packetId, Class<T> packetClass, Supplier<T> packetConstructor) {
            final Object oldId = this.classToId.put(packetClass, packetId);

            if (oldId != null) {
                this.idToPacket.remove(oldId);
            }

            this.idToPacket.put(packetId, packetConstructor);
        }

        @SuppressWarnings("unchecked")
        public <T extends Packet> T constructPacket(int id) {
            final Supplier<? extends Packet> constructor = this.idToPacket.get(id);

            if (constructor == null) {
                return null;
            }
            return (T) constructor.get();
        }

        public int id(Class<? extends Packet> packetClass) {
            final int id = this.classToId.getOrDefault(packetClass, -1);

            if (id == -1) {
                throw new SentinelException("Couldn't find a packet id for " + packetClass.getName() + " in protocol " + this.version);
            }
            return id;
        }

    }

    record PacketMapping(int packetId, ProtocolVersion fromVersion, ProtocolVersion toVersion) {}

    private static PacketMapping map(int packetId, ProtocolVersion fromVersion, ProtocolVersion toVersion) {
        return new PacketMapping(packetId, fromVersion, toVersion);
    }

    private static PacketMapping map(int packetId, ProtocolVersion version) {
        return new PacketMapping(packetId, version, MAXIMAL_VERSION);
    }

}
