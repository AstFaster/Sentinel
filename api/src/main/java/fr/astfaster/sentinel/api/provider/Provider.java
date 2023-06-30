package fr.astfaster.sentinel.api.provider;

import java.util.IdentityHashMap;
import java.util.Map;

public class Provider {

    private static final Map<Class<? extends Providable>, Providable> PROVIDABLE_MAP = new IdentityHashMap<>();

    public static void register(Class<? extends Providable> providableClass, Providable providable) {
        PROVIDABLE_MAP.put(providableClass, providable);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Providable> P provide(Class<? extends Providable> providableClass) {
        return (P) PROVIDABLE_MAP.get(providableClass);
    }

}
