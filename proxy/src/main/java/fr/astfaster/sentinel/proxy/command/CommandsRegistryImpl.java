package fr.astfaster.sentinel.proxy.command;

import fr.astfaster.sentinel.api.command.Command;
import fr.astfaster.sentinel.api.command.CommandMeta;
import fr.astfaster.sentinel.api.command.CommandsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class CommandsRegistryImpl implements CommandsRegistry {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, CommandMeta> commandsMeta = new HashMap<>();
    private final Map<String, Command> commands = new HashMap<>();

    @Override
    public void register(@NotNull Consumer<CommandMeta.Builder> metaBuilder, @NotNull Command command) {
        this.lock.writeLock().lock();

        final CommandMetaImpl.Builder builder = new CommandMetaImpl.Builder();

        metaBuilder.accept(builder);

        final CommandMeta meta = builder.build();

        this.commands.put(meta.label(), command);
        this.commandsMeta.put(meta.label(), meta);

        for (String alias : meta.aliases()) {
            this.commands.put(alias, command);
            this.commandsMeta.put(alias, meta);
        }

        this.lock.writeLock().unlock();
    }

    @Override
    public void unregister(@NotNull String commandLabel) {
        commandLabel = commandLabel.toLowerCase();

        this.lock.writeLock().lock();

        if (this.commands.remove(commandLabel) != null) {
            final CommandMeta meta = this.commandsMeta.remove(commandLabel);

            for (String alias : meta.aliases()) {
                this.commands.remove(alias);
                this.commandsMeta.remove(alias);
            }
        }

        this.lock.writeLock().unlock();
    }

    @Override
    public Command command(@NotNull String commandLabel) {
        this.lock.readLock().lock();

        try {
            return this.commands.get(commandLabel.toLowerCase());
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public CommandMeta commandMeta(@NotNull String commandLabel) {
        this.lock.readLock().lock();

        try {
            return this.commandsMeta.get(commandLabel.toLowerCase());
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Command> commands() {
        this.lock.readLock().lock();

        try {
            return this.commands;
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
