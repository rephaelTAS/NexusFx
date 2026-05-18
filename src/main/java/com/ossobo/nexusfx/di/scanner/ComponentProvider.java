package com.ossobo.nexusfx.di.scanner;

@FunctionalInterface
public interface ComponentProvider<T> {
    T get();
}
