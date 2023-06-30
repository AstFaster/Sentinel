package fr.astfaster.sentinel.api.command;

import org.jetbrains.annotations.NotNull;

public interface CommandMeta {

    String label();

    String[] aliases();

    interface Builder {

        Builder label(@NotNull String label);

        Builder aliases(@NotNull String... aliases);

    }

}
