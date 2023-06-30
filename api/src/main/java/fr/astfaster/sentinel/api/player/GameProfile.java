package fr.astfaster.sentinel.api.player;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface GameProfile {

    UUID id();

    void id(@NotNull UUID id);

    String name();

    void name(@NotNull String name);

    List<Property> properties();

    void properties(@NotNull List<Property> properties);

    void newProperty(@NotNull String name, @NotNull String value, String signature);

    interface Property {

        String name();

        String value();

        String signature();

    }

}
