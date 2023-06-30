package fr.astfaster.sentinel.api.event;

public interface ResultingEvent<T> extends Event {

    T result();

    void result(T result);

}
