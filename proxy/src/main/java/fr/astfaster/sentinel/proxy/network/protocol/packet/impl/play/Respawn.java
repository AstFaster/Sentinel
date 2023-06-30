package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.Location;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public class Respawn implements Packet {

    private int dimension;
    private String levelName;
    private String levelType;
    private String dimensionIdentifier;
    private CompoundBinaryTag dimensionData;

    private long seed;
    private short difficulty;
    private short gameMode;
    private short previousGameMode;
    private boolean debug;
    private boolean flat;
    private byte dataToKeep;
    private Location deathLocation;
    private int portalCooldown;

    public Respawn() {}

    public Respawn(int dimension, String levelName, String levelType, String dimensionIdentifier,
                   CompoundBinaryTag dimensionData, long seed, short difficulty, short gameMode,
                   short previousGameMode, boolean debug, boolean flat, byte dataToKeep, Location deathLocation,
                   int portalCooldown) {
        this.dimension = dimension;
        this.levelName = levelName;
        this.levelType = levelType;
        this.dimensionIdentifier = dimensionIdentifier;
        this.dimensionData = dimensionData;
        this.seed = seed;
        this.difficulty = difficulty;
        this.gameMode = gameMode;
        this.previousGameMode = previousGameMode;
        this.debug = debug;
        this.flat = flat;
        this.dataToKeep = dataToKeep;
        this.deathLocation = deathLocation;
        this.portalCooldown = portalCooldown;
    }

    public static Respawn of(JoinGame join) {
        return new Respawn(join.dimension(), join.levelName(), join.levelType(), join.dimensionIdentifier(),
                join.dimensionData(), join.seed(), join.difficulty(), join.gamemode(), join.previousGamemode(),
                join.debug(), join.flat(), (byte) 0, join.deathLocation(), join.portalCooldown());
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            if (version.moreOrEqual(ProtocolVersion.V_1_16_2) && version.less(ProtocolVersion.V_1_19)) {
                this.dimensionData = buffer.readTag();
            } else {
                this.dimensionIdentifier = buffer.readString();
            }

            this.levelName = buffer.readString();
        } else {
            this.dimension = buffer.readInt();
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            this.seed = buffer.readLong();
        }

        if (version.lessOrEqual(ProtocolVersion.V_1_13_2)) {
            this.difficulty = buffer.readUnsignedByte();
        }

        this.gameMode = buffer.readUnsignedByte();

        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            this.previousGameMode = buffer.readUnsignedByte();
            this.debug = buffer.readBoolean();
            this.flat = buffer.readBoolean();

            if (version.moreOrEqual(ProtocolVersion.V_1_19_3)) {
                this.dataToKeep = buffer.readByte();
            } else {
                this.dataToKeep = buffer.readBoolean() ? (byte) 1 : (byte) 0;
            }
        } else {
            this.levelType = buffer.readString();
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_19)) {
            if (buffer.readBoolean()) {
                this.deathLocation = new Location(buffer.readString(), buffer.readLong());
            }
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_20)) {
            this.portalCooldown = buffer.readVarInt();
        }
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            if (version.moreOrEqual(ProtocolVersion.V_1_16_2) && version.less(ProtocolVersion.V_1_19)) {
                buffer.writeTag(this.dimensionData);
            } else {
                buffer.writeString(this.dimensionIdentifier);
            }

            buffer.writeString(this.levelName);
        } else {
            buffer.writeInt(this.dimension);
        }

        if (version.lessOrEqual(ProtocolVersion.V_1_13_2)) {
            buffer.writeByte(this.difficulty);
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            buffer.writeLong(this.seed);
        }

        buffer.writeByte(this.gameMode);

        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            buffer.writeByte(this.previousGameMode);
            buffer.writeBoolean(this.debug);
            buffer.writeBoolean(this.flat);

            if (version.moreOrEqual(ProtocolVersion.V_1_19_3)) {
                buffer.writeByte(this.dataToKeep);
            } else {
                buffer.writeBoolean(this.dataToKeep != 0);
            }
        } else {
            buffer.writeString(this.levelType);
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_19)) {
            buffer.writeBoolean(this.deathLocation != null);

            if (this.deathLocation != null) {
                buffer.writeString(this.deathLocation.dimension());
                buffer.writeLong(this.deathLocation.position());
            }
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_20)) {
            buffer.writeVarInt(this.portalCooldown);
        }
    }

}
