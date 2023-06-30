package fr.astfaster.sentinel.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface EventBus {

    void subscribe(@NotNull Object subscriber);

    <T extends Event> void subscribe(@NotNull Class<T> eventClass, @NotNull EventSubscriber<T> subscriber);

    void unsubscribe(@NotNull Object subscriber);

    <T extends Event> void unsubscribe(@NotNull Class<T> eventClass, @NotNull EventSubscriber<T> subscriber);

    <T extends Event> CompletableFuture<T> publish(@NotNull T event);

}
