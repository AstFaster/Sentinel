package fr.astfaster.sentinel.proxy.network.protocol.packet.impl;

import fr.astfaster.sentinel.api.network.protocol.ProtocolVersion;
import fr.astfaster.sentinel.proxy.network.protocol.NotchianBuffer;
import fr.astfaster.sentinel.proxy.network.protocol.packet.Packet;
import fr.astfaster.sentinel.proxy.util.Serializers;
import net.kyori.adventure.text.Component;

public class Disconnect implements Packet {

    private String json;

    public Disconnect() {}

    public Disconnect(String json) {
        this.json = json;
    }

    @Override
    public void read(ProtocolVersion version, NotchianBuffer buffer) {
        this.json = buffer.readString();
    }

    @Override
    public void write(ProtocolVersion version, NotchianBuffer buffer) {
        buffer.writeString(this.json);
    }

    public String json() {
        return this.json;
    }

    public Component reason() {
        return Serializers.MESSAGE_SERIALIZER.fromJson(json, Component.class);
    }

}
