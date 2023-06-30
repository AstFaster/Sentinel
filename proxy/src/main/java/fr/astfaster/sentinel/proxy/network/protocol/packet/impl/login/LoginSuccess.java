package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.login;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.api.player.GameProfile;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.player.GameProfileImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginSuccess implements Packet {

    private UUID uuid;
    private String username;
    private List<GameProfile.Property> properties = new ArrayList<>();

    public LoginSuccess(UUID uuid, String username, List<GameProfile.Property> properties) {
        this.uuid = uuid;
        this.username = username;
        this.properties = properties;
    }

    public LoginSuccess(GameProfile profile) {
        this(profile.id(), profile.name(), profile.properties());
    }

    public LoginSuccess() {}

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            this.uuid = buffer.readUUID();
        } else {
            this.uuid = UUID.fromString(buffer.readString());
        }

        this.username = buffer.readString(16);

        if (version.moreOrEqual(ProtocolVersion.V_1_19)) {
            final int propertiesSize = buffer.readVarInt();

            for (int i = 0; i < propertiesSize; i++) {
                final String name = buffer.readString();
                final String value = buffer.readString();
                final String signature = buffer.readBoolean() ? buffer.readString() : null;

                this.properties.add(new GameProfileImpl.Property(name, value, signature));
            }
        }
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            buffer.writeUUID(this.uuid);
        } else {
            buffer.writeString(this.uuid.toString());
        }

        buffer.writeString(this.username);

        if (version.moreOrEqual(ProtocolVersion.V_1_19)) {
            buffer.writeVarInt(this.properties.size());

            for (GameProfile.Property property : this.properties) {
                buffer.writeString(property.name());
                buffer.writeString(property.value());

                final boolean signed = property.signature() != null;

                buffer.writeBoolean(signed);

                if (signed) {
                    buffer.writeString(property.signature());
                }
            }
        }
    }

    public UUID uuid() {
        return this.uuid;
    }

    public String username() {
        return this.username;
    }

    public List<GameProfile.Property> properties() {
        return this.properties;
    }

}