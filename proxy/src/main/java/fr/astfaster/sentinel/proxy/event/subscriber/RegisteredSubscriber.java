package fr.astfaster.sentinel.proxy.event.subscriber;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.event.EventExecution;

public interface RegisteredSubscriber {

    Type type();

    Class<? extends Event> subscribedEvent();

    int order();

    void execute(Event event, EventExecution execution);

    enum Type {

        NORMAL,
        EXECUTION_CONTROL

    }

}
