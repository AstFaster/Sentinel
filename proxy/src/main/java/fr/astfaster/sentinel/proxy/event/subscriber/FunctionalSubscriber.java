package fr.astfaster.sentinel.proxy.event.subscriber;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.event.EventExecution;
import fr.astfaster.sentinel.api.event.EventOrder;
import fr.astfaster.sentinel.api.event.EventSubscriber;

public class FunctionalSubscriber implements RegisteredSubscriber {

    private final Type type;
    private final Class<? extends Event> subscribedEvent;
    private final EventSubscriber<?> handle;

    public FunctionalSubscriber(Type type, Class<? extends Event> subscribedEvent, EventSubscriber<?> handle) {
        this.type = type;
        this.subscribedEvent = subscribedEvent;
        this.handle = handle;
    }

    public EventSubscriber<?> handle() {
        return this.handle;
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public Class<? extends Event> subscribedEvent() {
        return this.subscribedEvent;
    }

    @Override
    public int order() {
        return EventOrder.DEFAULT;
    }

    @Override
    public void execute(Event event, EventExecution execution) {}

}
