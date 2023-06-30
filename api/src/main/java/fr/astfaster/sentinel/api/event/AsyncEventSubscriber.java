package fr.astfaster.sentinel.api.event;

import fr.astfaster.sentinel.api.util.SentinelException;

public interface AsyncEventSubscriber<T extends Event> extends EventSubscriber<T> {

    @Override
    default void handle(T event) {
        throw new SentinelException("You can't use #handle method for an AsyncEventSubscriber!");
    }

    @Override
    void handleAsync(T event, EventExecution execution);

}
