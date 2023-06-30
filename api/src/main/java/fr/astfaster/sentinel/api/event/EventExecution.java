package fr.astfaster.sentinel.api.event;

public interface EventExecution {

    void async(Runnable task);

    void resume();

}
