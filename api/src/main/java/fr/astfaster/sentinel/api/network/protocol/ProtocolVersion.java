package fr.astfaster.sentinel.api.network.protocol;

import java.util.*;

public enum ProtocolVersion {

    UNKNOWN(-1, "Unknown"),
    V_1_8(47, "1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"), // Same for all 1.8 versions

    V_1_9(107, "1.9"),
    V_1_9_1(108, "1.9.1"),
    V_1_9_2(109, "1.9.2"),
    V_1_9_4(110, "1.9.3", "1.9.4"), // Same as 1.9.3

    V_1_10(210, "1.10", "1.10.1", "1.10.2"), // Same for all 1.10 versions

    V_1_11(315, "1.11"),
    V_1_11_2(316, "1.11.1", "1.11.2"), // Same as 1.11.1

    V_1_12(335, "1.12"),
    V_1_12_1(338, "1.12.1"),
    V_1_12_2(340, "1.12.2"),

    V_1_13(393, "1.13"),
    V_1_13_1(401, "1.13.1"),
    V_1_13_2(404, "1.13.2"),

    V_1_14(477, "1.14"),
    V_1_14_1(480, "1.14.1"),
    V_1_14_2(485, "1.14.2"),
    V_1_14_3(490, "1.14.3"),
    V_1_14_4(498, "1.14.4"),

    V_1_15(573, "1.15"),
    V_1_15_1(575, "1.15.1"),
    V_1_15_2(578, "1.15.2"),

    V_1_16(735, "1.16"),
    V_1_16_1(736, "1.16.1"),
    V_1_16_2(751, "1.16.2"),
    V_1_16_3(753, "1.16.3"),
    V_1_16_5(754, "1.16.4", "1.16.5"), // Same as 1.16.4

    V_1_17(755, "1.17"),
    V_1_17_1(756, "1.17.1"),

    V_1_18(757, "1.18"),
    V_1_18_2(758, "1.18.2"),

    V_1_19(759, "1.19"),
    V_1_19_1(760, "1.19.1", "1.19.2"), // Same as 1.19.2
    V_1_19_3(761, "1.19.3"),
    V_1_19_4(762, "1.19.4"),

    V_1_20(763, "1.20", "1.20.1"), // Same as 1.20.1

    ;

    public static final ProtocolVersion MINIMAL_VERSION = ProtocolVersion.V_1_8;
    public static final ProtocolVersion MAXIMAL_VERSION = values()[values().length - 1];

    private static final Map<Integer, ProtocolVersion> BY_PROTOCOL = new HashMap<>();

    static {
        for (ProtocolVersion version : ProtocolVersion.values()) {
            if (version.unknown()) {
                continue;
            }

            BY_PROTOCOL.put(version.protocol(), version);
        }
    }

    private final int protocol;
    private final String[] names;

    ProtocolVersion(int protocol, String... names) {
        this.protocol = protocol;
        this.names = names;
    }

    public boolean more(ProtocolVersion version) {
        return this.protocol > version.protocol();
    }

    public boolean moreOrEqual(ProtocolVersion version) {
        return this.protocol >= version.protocol();
    }

    public boolean less(ProtocolVersion version) {
        return this.protocol < version.protocol();
    }

    public boolean lessOrEqual(ProtocolVersion version) {
        return this.protocol <= version.protocol();
    }

    public int protocol() {
        return this.protocol;
    }

    public boolean unknown() {
        return this == UNKNOWN;
    }

    public String[] names() {
        return this.names;
    }

    public static ProtocolVersion from(int protocol) {
        return BY_PROTOCOL.get(protocol);
    }

    public static EnumSet<ProtocolVersion> between(ProtocolVersion min, ProtocolVersion max) {
        return EnumSet.range(min, max);
    }

}
