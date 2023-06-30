package fr.astfaster.sentinel.proxy.event.subscriber;

import fr.astfaster.sentinel.api.event.Event;
import fr.astfaster.sentinel.api.event.EventExecution;
import fr.astfaster.sentinel.proxy.event.DefaultEventBus;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodSubscriber implements RegisteredSubscriber {

    private final Type type;
    private final Class<? extends Event> subscribedEvent;
    private final int priority;
    private final Method method;
    private final Object methodObject;

    public MethodSubscriber(Type type, Class<? extends Event> subscribedEvent, int priority, Method method, Object methodObject) {
        this.type = type;
        this.subscribedEvent = subscribedEvent;
        this.priority = priority;
        this.method = method;
        this.methodObject = methodObject;
    }

    public Object methodObject() {
        return this.methodObject;
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
        return this.priority;
    }

    @Override
    public void execute(Event event, EventExecution execution) {
        try {
            if (this.type == Type.EXECUTION_CONTROL) {
                this.method.invoke(this.methodObject, event, execution);
            } else if (this.type == Type.NORMAL){
                this.method.invoke(this.methodObject, event);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogManager.getLogger(DefaultEventBus.class).error("Couldn't execute {} method in {} class!", this.method.getName(), this.methodObject.getClass(), e);
        }
    }

}
