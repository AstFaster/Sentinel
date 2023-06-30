package fr.astfaster.sentinel.proxy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.astfaster.sentinel.proxy.network.protocol.ProxyPingImpl;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class Serializers {

    public static final Gson PING_SERIALIZER = GsonComponentSerializer.builder()
            .build()
            .serializer()
            .newBuilder()
            .registerTypeHierarchyAdapter(ProxyPingImpl.class, new ProxyPingImpl.Serializer())
            .create();

    public static final Gson MESSAGE_SERIALIZER = GsonComponentSerializer.builder()
            .build()
            .serializer()
            .newBuilder()
            .create();

    public static final Gson GLOBAL_SERIALIZER = new GsonBuilder()
            .create();

}
