package fr.astfaster.sentinel.api.event;

@FunctionalInterface
public interface EventSubscriber<T extends Event> {

    void handle(T event);

    default void handleAsync(T event, EventExecution execution) {
        this.handle(event);
    }

}
