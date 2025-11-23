package net.fameless.core.config.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeAdapterRegistry {

    private final Map<Class<?>, TypeAdapter<?>> adapters = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, TypeAdapter<T> adapter) {
        adapters.put(type, adapter);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return (TypeAdapter<T>) adapters.get(type);
    }

}
