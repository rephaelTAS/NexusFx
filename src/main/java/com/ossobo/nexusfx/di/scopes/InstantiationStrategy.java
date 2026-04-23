package com.ossobo.nexusfx.di.scopes;

import com.ossobo.nexusfx.di.DiContainer;

@FunctionalInterface
public interface InstantiationStrategy<T> {
    T getInstance(DiContainer container);
}