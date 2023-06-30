package fr.astfaster.sentinel.proxy.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fr.astfaster.sentinel.api.player.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class GameProfileImpl implements GameProfile {

    private UUID id;
    private String name;
    private List<GameProfile.Property> properties;

    public GameProfileImpl(UUID id, String name, List<GameProfile.Property> properties) {
        this.id = id;
        this.name = name;
        this.properties = properties;
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public void id(@NotNull UUID id) {
        this.id = id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void name(@NotNull String name) {
        this.name = name;
    }

    @Override
    public List<GameProfile.Property> properties() {
        return this.properties;
    }

    @Override
    public void properties(@NotNull List<GameProfile.Property> properties) {
        this.properties = properties;
    }

    @Override
    public void newProperty(@NotNull String name, @NotNull String value, @NotNull String signature) {
        this.properties.add(new Property(name, value, signature));
    }

    public record Property(String name, String value, String signature) implements GameProfile.Property {}

}
