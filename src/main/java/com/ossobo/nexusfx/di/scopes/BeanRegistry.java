package com.ossobo.nexusfx.di.scopes;


import java.util.function.Supplier;

interface BeanRegistry { // Acesso default, pois é interna ao pacote principal do DIContainer
    void registerBean(Class<?> clazz);
    void registerBean(String name, Class<?> clazz);
    void registerBean(String name, Class<?> clazz, ScopeType scopeType);
    <T> void registerSingleton(String name, T instance);
    <T> void registerSingleton(T instance);
    <T> void registerBean(Class<T> type, Supplier<T> supplier); // Para registro baseado em fábrica
}