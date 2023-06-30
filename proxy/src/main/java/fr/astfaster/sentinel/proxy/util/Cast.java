package fr.astfaster.sentinel.proxy.util;

public interface Cast<T> {

    @SuppressWarnings("unchecked")
    default <C extends T> C cast() {
        return (C) this;
    }

}
