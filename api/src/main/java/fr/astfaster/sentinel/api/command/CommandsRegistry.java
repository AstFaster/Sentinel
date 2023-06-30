package fr.astfaster.sentinel.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public interface CommandsRegistry {

    void register(@NotNull Consumer<CommandMeta.Builder> metaBuilder, @NotNull Command command);

    void unregister(@NotNull String commandLabel);

    Command command(@NotNull String commandLabel);

    CommandMeta commandMeta(@NotNull String commandLabel);

    Map<String, Command> commands();

}
