package net.fameless.core.config.adapter;

public interface TypeAdapter<T> {
    T adapt(Object value);
}
