package fr.astfaster.sentinel.proxy.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import fr.astfaster.sentinel.api.event.*;
import fr.astfaster.sentinel.api.util.SentinelException;
import fr.astfaster.sentinel.proxy.event.subscriber.FunctionalSubscriber;
import fr.astfaster.sentinel.proxy.event.subscriber.MethodSubscriber;
import fr.astfaster.sentinel.proxy.event.subscriber.RegisteredSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static fr.astfaster.sentinel.proxy.event.subscriber.RegisteredSubscriber.*;

public class DefaultEventBus implements EventBus {

    private final Logger logger = LogManager.getLogger(DefaultEventBus.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Set<RegisteredSubscriber> subscribers = ConcurrentHashMap.newKeySet();

    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Sentinel Async Events - #%d")
            .build());

    @SuppressWarnings("unchecked")
    @Override
    public void subscribe(@NotNull Object subscriber) {
        final Set<Method> methods = new HashSet<>();

        methods.addAll(Arrays.asList(subscriber.getClass().getDeclaredMethods()));
        methods.addAll(Arrays.asList(subscriber.getClass().getMethods()));

        this.lock.writeLock().lock();

        for (Method method : methods) {
            final Subscriber annotation = method.getAnnotation(Subscriber.class);

            if (annotation != null) {
                final Class<?>[] parameters = method.getParameterTypes();

                if (parameters.length != 1 && parameters.length != 2) {
                    this.logger.warn("{} method in {} class was annotated with {} but doesn't have 1 or 2 parameters!", method.getName(), subscriber.getClass(), annotation);
                    continue;
                }

                final Class<?> firstParameter = parameters[0];

                if (!Event.class.isAssignableFrom(firstParameter)) {
                    this.logger.warn("{} method in {} class was annotated with {} but has an invalid Event parameter!", method.getName(), subscriber.getClass(), annotation);
                    continue;
                }

                Type subscriberType = Type.NORMAL;

                if (parameters.length == 2) {
                    final Class<?> secondParameter = parameters[1];

                    if (secondParameter != EventExecution.class) {
                        this.logger.warn("{} method in {} class was annotated with {} but has an invalid EventExecution parameter!", method.getName(), subscriber.getClass(), annotation);
                        continue;
                    }

                    subscriberType = Type.EXECUTION_CONTROL;
                }

                final Class<? extends Event> eventClass = (Class<? extends Event>) firstParameter;
                final MethodSubscriber registeredSubscriber = new MethodSubscriber(subscriberType, eventClass, annotation.order(), method, subscriber);

                method.setAccessible(true);

                this.subscribers.add(registeredSubscriber);
            }
        }

        this.lock.writeLock().unlock();
    }

    @Override
    public <T extends Event> void subscribe(@NotNull Class<T> eventClass, @NotNull EventSubscriber<T> subscriber) {
        this.lock.writeLock().lock();

        final Type type = subscriber instanceof AsyncEventSubscriber<T> ? Type.EXECUTION_CONTROL : Type.NORMAL;
        final FunctionalSubscriber registeredSubscriber = new FunctionalSubscriber(type, eventClass, subscriber) {
            @Override
            public void execute(Event event, EventExecution execution) {
                subscriber.handleAsync(eventClass.cast(event), execution);
            }
        };

        this.subscribers.add(registeredSubscriber);

        this.lock.writeLock().unlock();
    }

    @Override
    public void unsubscribe(@NotNull Object subscriber) {
        this.lock.writeLock().lock();

        for (RegisteredSubscriber registeredSubscriber : Set.copyOf(this.subscribers)) {
            if (registeredSubscriber instanceof final MethodSubscriber methodSubscriber) {
                if (!methodSubscriber.methodObject().equals(subscriber)) {
                    continue;
                }

                this.subscribers.remove(registeredSubscriber);
            }
        }

        this.lock.writeLock().unlock();
    }

    @Override
    public <T extends Event> void unsubscribe(@NotNull Class<T> eventClass, @NotNull EventSubscriber<T> subscriber) {
        this.lock.writeLock().lock();

        for (RegisteredSubscriber registeredSubscriber : Set.copyOf(this.subscribers)) {
            if (registeredSubscriber instanceof final FunctionalSubscriber methodSubscriber) {
                if (!methodSubscriber.handle().equals(subscriber)) {
                    continue;
                }

                this.subscribers.remove(registeredSubscriber);
            }
        }

        this.lock.writeLock().unlock();
    }

    @Override
    public <T extends Event> CompletableFuture<T> publish(@NotNull T event) {
        this.lock.readLock().lock();

        try {
            final List<RegisteredSubscriber> subscribers = this.subscribers.stream()
                    .filter(subscriber -> subscriber.subscribedEvent().isAssignableFrom(event.getClass()))
                    .toList();

            if (subscribers.size() == 0) {
                return CompletableFuture.completedFuture(event);
            }

            final CompletableFuture<T> future = new CompletableFuture<>();
            final List<RegisteredSubscriber> sortedSubscribers = subscribers.stream()
                    .sorted(Comparator.comparingInt(RegisteredSubscriber::order))
                    .toList();
            final Execution<T> execution = new Execution<>(event, sortedSubscribers, future);

            execution.execute();

            return future;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    class Execution<T extends Event> implements EventExecution {

        private int currentIndex = 0;

        private final AtomicBoolean waiting = new AtomicBoolean(false);

        private final T event;
        private final List<RegisteredSubscriber> subscribers;
        private final CompletableFuture<T> future;

        public Execution(T event, List<RegisteredSubscriber> subscribers, CompletableFuture<T> future) {
            this.event = event;
            this.subscribers = subscribers;
            this.future = future;
        }

        void execute() {
            for (int i = this.currentIndex; i < this.subscribers.size(); i++) {
                final RegisteredSubscriber subscriber = this.subscribers.get(i);
                final Type type = subscriber.type();

                this.currentIndex++;

                if (type == Type.EXECUTION_CONTROL) {
                    this.waiting.set(true);

                    subscriber.execute(this.event, this);
                    return;
                } else if (type == Type.NORMAL) {
                    subscriber.execute(this.event, this);
                }
            }

            this.end();
        }

        private void end() {
            this.future.complete(this.event);
        }

        @Override
        public void async(Runnable task) {
            asyncExecutor.execute(task);
        }

        @Override
        public void resume() {
            if (!this.waiting.get()) {
                throw new SentinelException("Couldn't resume a non-waiting EventExecution!");
            }

            this.waiting.set(false);

            if (this.currentIndex < this.subscribers.size()) {
                this.execute();
            } else {
                this.end();
            }
        }

    }

}
