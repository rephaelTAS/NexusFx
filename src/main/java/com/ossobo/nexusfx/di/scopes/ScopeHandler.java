package com.ossobo.nexusfx.di.scopes;

public interface ScopeHandler {
    <T> T get(Class<T> type, java.util.function.Supplier<T> creator);
    void remove(Class<?> type);

    void clear();
}