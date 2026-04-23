package com.ossobo.nexusfx.di.scopes;

public interface DependencyLifecycleListener {
    void onEvent(Class<?> type, String event);
}