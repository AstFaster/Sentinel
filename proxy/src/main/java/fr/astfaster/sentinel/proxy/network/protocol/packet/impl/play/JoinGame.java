package fr.astfaster.sentinel.proxy.network.protocol.packet.impl.play;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.Location;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.Set;

public class JoinGame implements Packet {

    private static final BinaryTagIO.Reader TAG_READER = BinaryTagIO.reader(4 * 1024 * 1024);

    private int entityId;

    private short gamemode;
    private short previousGamemode; // 1.16+

    private int dimension;
    private Set<String> levelNames; // 1.16+
    private String levelName;
    private String levelType;
    private String dimensionIdentifier;
    private CompoundBinaryTag dimensions; // 1.16+
    private CompoundBinaryTag dimensionData; // 1.16.2+

    private long seed; // 1.15+

    private boolean debug;
    private boolean flat;

    private boolean reducedDebugInfo;
    private boolean respawnScreen;

    private int viewDistance; // 1.14+
    private int simulationDistance; // 1.18+

    private short difficulty;
    private boolean hardcore;
    private int maxPlayers;

    private Location deathLocation; // 1.19+
    private int portalCooldown; // 1.20+

    public JoinGame() {}

    public JoinGame(int entityId, short gamemode, short previousGamemode, int dimension, Set<String> levelNames,
                    String levelName, String levelType, String dimensionIdentifier, CompoundBinaryTag dimensions,
                    CompoundBinaryTag dimensionData, long seed, boolean debug, boolean flat, boolean reducedDebugInfo,
                    boolean respawnScreen, int viewDistance, int simulationDistance, short difficulty, boolean hardcore,
                    int maxPlayers, Location deathLocation, int portalCooldown) {
        this.entityId = entityId;
        this.gamemode = gamemode;
        this.previousGamemode = previousGamemode;
        this.dimension = dimension;
        this.levelNames = levelNames;
        this.levelName = levelName;
        this.levelType = levelType;
        this.dimensionIdentifier = dimensionIdentifier;
        this.dimensions = dimensions;
        this.dimensionData = dimensionData;
        this.seed = seed;
        this.debug = debug;
        this.flat = flat;
        this.reducedDebugInfo = reducedDebugInfo;
        this.respawnScreen = respawnScreen;
        this.viewDistance = viewDistance;
        this.simulationDistance = simulationDistance;
        this.difficulty = difficulty;
        this.hardcore = hardcore;
        this.maxPlayers = maxPlayers;
        this.deathLocation = deathLocation;
        this.portalCooldown = portalCooldown;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            this.readModern(version, buffer);
        } else {
            this.readLegacy(version, buffer);
        }
    }

    private void readLegacy(ProtocolVersion version, NotchianBuffer buffer) {
        this.entityId = buffer.readInt();
        this.gamemode = buffer.readByte();
        this.hardcore = (this.gamemode & 0x08) != 0;
        this.gamemode &= ~0x08;
        this.dimension = version.moreOrEqual(ProtocolVersion.V_1_9_1) ? buffer.readInt() : buffer.readByte();

        if (version.lessOrEqual(ProtocolVersion.V_1_13_2)) {
            this.difficulty = buffer.readUnsignedByte();
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            this.seed = buffer.readLong();
        }

        this.maxPlayers = buffer.readUnsignedByte();
        this.levelType = buffer.readString(16);

        if (version.moreOrEqual(ProtocolVersion.V_1_14)) {
            this.viewDistance = buffer.readVarInt();
        }

        this.reducedDebugInfo = buffer.readBoolean();

        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            this.respawnScreen = buffer.readBoolean();
        }
    }

    private void readModern(ProtocolVersion version, NotchianBuffer buffer) {
        this.entityId = buffer.readInt();

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2)) {
            this.hardcore = buffer.readBoolean();
            this.gamemode = buffer.readByte();
        } else {
            this.gamemode = buffer.readByte();
            this.hardcore = (this.gamemode & 0x08) != 0;
            this.gamemode &= ~0x08;
        }

        this.previousGamemode = buffer.readByte();
        this.levelNames = ImmutableSet.copyOf(buffer.readStringsArray());
        this.dimensions = buffer.readTag(TAG_READER);

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2) && version.less(ProtocolVersion.V_1_19)) {
            this.dimensionData = buffer.readTag(TAG_READER);
        } else {
            this.dimensionIdentifier = buffer.readString();
        }

        this.levelName = buffer.readString();
        this.seed = buffer.readLong();

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2)) {
            this.maxPlayers = buffer.readVarInt();
        } else {
            this.maxPlayers = buffer.readUnsignedByte();
        }

        this.viewDistance = buffer.readVarInt();

        if (version.moreOrEqual(ProtocolVersion.V_1_18)) {
            this.simulationDistance = buffer.readVarInt();
        }

        this.reducedDebugInfo = buffer.readBoolean();
        this.respawnScreen = buffer.readBoolean();
        this.debug = buffer.readBoolean();
        this.flat = buffer.readBoolean();

        if (version.more(ProtocolVersion.V_1_19) && buffer.readBoolean()) {
            this.deathLocation = new Location(buffer.readString(), buffer.readLong());
        }

        if (version.more(ProtocolVersion.V_1_20)) {
            this.portalCooldown = buffer.readVarInt();
        }
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        if (version.moreOrEqual(ProtocolVersion.V_1_16)) {
            this.encodeModern(version, buffer);
        } else {
            this.encodeLegacy(version, buffer);
        }
    }

    private void encodeLegacy(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeByte(this.hardcore ? this.gamemode | 0x8 : this.gamemode);

        if (version.moreOrEqual(ProtocolVersion.V_1_9_1)) {
            buffer.writeInt(this.dimension);
        } else {
            buffer.writeByte(this.dimension);
        }

        if (version.lessOrEqual(ProtocolVersion.V_1_13_2)) {
            buffer.writeByte(this.difficulty);
        }

        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            buffer.writeLong(this.seed);
        }

        buffer.writeByte(maxPlayers);
        buffer.writeString(levelType);

        if (version.moreOrEqual(ProtocolVersion.V_1_14)) {
            buffer.writeVarInt(this.viewDistance);
        }
        
        if (version.moreOrEqual(ProtocolVersion.V_1_8)) {
            buffer.writeBoolean(this.reducedDebugInfo);
        }
        
        if (version.moreOrEqual(ProtocolVersion.V_1_15)) {
            buffer.writeBoolean(this.respawnScreen);
        }
    }

    private void encodeModern(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeInt(this.entityId);

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2)) {
            buffer.writeBoolean(this.hardcore);
            buffer.writeByte(this.gamemode);
        } else {
            buffer.writeByte(this.hardcore ? this.gamemode | 0x8 : this.gamemode);
        }

        buffer.writeByte(this.previousGamemode);

        buffer.writeStringsArray(this.levelNames.toArray(String[]::new));
        buffer.writeTag(this.dimensions);

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2) && version.less(ProtocolVersion.V_1_19)) {
            buffer.writeTag(this.dimensionData);
        } else {
            buffer.writeString(this.dimensionIdentifier);
        }

        buffer.writeString(this.levelName);
        buffer.writeLong(this.seed);

        if (version.moreOrEqual(ProtocolVersion.V_1_16_2)) {
            buffer.writeVarInt(this.maxPlayers);
        } else {
            buffer.writeByte(this.maxPlayers);
        }

        buffer.writeVarInt(this.viewDistance);

        if (version.moreOrEqual(ProtocolVersion.V_1_18)) {
            buffer.writeVarInt(this.simulationDistance);
        }

        buffer.writeBoolean(this.reducedDebugInfo);
        buffer.writeBoolean(this.respawnScreen);
        buffer.writeBoolean(this.debug);
        buffer.writeBoolean(this.flat);

        // optional death location
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

    public int entityId() {
        return this.entityId;
    }

    public void entityId(int entityId) {
        this.entityId = entityId;
    }

    public short gamemode() {
        return this.gamemode;
    }

    public void gamemode(short gamemode) {
        this.gamemode = gamemode;
    }

    public short previousGamemode() {
        return this.previousGamemode;
    }

    public void previousGamemode(short previousGamemode) {
        this.previousGamemode = previousGamemode;
    }

    public int dimension() {
        return this.dimension;
    }

    public void dimension(int dimension) {
        this.dimension = dimension;
    }

    public Set<String> levelNames() {
        return this.levelNames;
    }

    public void levelNames(Set<String> levelNames) {
        this.levelNames = levelNames;
    }

    public String levelName() {
        return this.levelName;
    }

    public void levelName(String levelName) {
        this.levelName = levelName;
    }

    public String levelType() {
        return this.levelType;
    }

    public void levelType(String levelType) {
        this.levelType = levelType;
    }

    public String dimensionIdentifier() {
        return this.dimensionIdentifier;
    }

    public void dimensionIdentifier(String dimensionIdentifier) {
        this.dimensionIdentifier = dimensionIdentifier;
    }

    public CompoundBinaryTag dimensions() {
        return this.dimensions;
    }

    public void dimensions(CompoundBinaryTag dimensions) {
        this.dimensions = dimensions;
    }

    public CompoundBinaryTag dimensionData() {
        return this.dimensionData;
    }

    public void dimensionData(CompoundBinaryTag dimensionData) {
        this.dimensionData = dimensionData;
    }

    public long seed() {
        return this.seed;
    }

    public void seed(long seed) {
        this.seed = seed;
    }

    public boolean debug() {
        return this.debug;
    }

    public void debug(boolean debug) {
        this.debug = debug;
    }

    public boolean flat() {
        return this.flat;
    }

    public void flat(boolean flat) {
        this.flat = flat;
    }

    public boolean reducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void reducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public boolean respawnScreen() {
        return this.respawnScreen;
    }

    public void respawnScreen(boolean respawnScreen) {
        this.respawnScreen = respawnScreen;
    }

    public int viewDistance() {
        return this.viewDistance;
    }

    public void viewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public int simulationDistance() {
        return this.simulationDistance;
    }

    public void simulationDistance(int simulationDistance) {
        this.simulationDistance = simulationDistance;
    }

    public short difficulty() {
        return this.difficulty;
    }

    public void difficulty(short difficulty) {
        this.difficulty = difficulty;
    }

    public boolean hardcore() {
        return this.hardcore;
    }

    public void hardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public int maxPlayers() {
        return this.maxPlayers;
    }

    public void maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Location deathLocation() {
        return this.deathLocation;
    }

    public void deathLocation(Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    public int portalCooldown() {
        return this.portalCooldown;
    }

    public void portalCooldown(int portalCooldown) {
        this.portalCooldown = portalCooldown;
    }

}
