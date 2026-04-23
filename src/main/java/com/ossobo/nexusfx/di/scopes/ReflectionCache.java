package com.ossobo.nexusfx.di.scopes;

import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.di.annotations.PostConstruct;
import com.ossobo.nexusfx.di.annotations.PreDestroy;
import com.ossobo.nexusfx.di.exceptions.DependencyInjectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Cache para informações de reflexão de classes, otimizando o acesso a construtores, campos e métodos.
 */
class ReflectionCache { // Acesso default
    private final Map<Class<?>, Constructor<?>> autowireConstructorsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> injectableFieldsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> postConstructMethodsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> preDestroyMethodsCache = new ConcurrentHashMap<>();

    public Constructor<?> getAutowireConstructor(Class<?> type) {
        return autowireConstructorsCache.computeIfAbsent(type, this::findAutowireConstructor);
    }

    public List<Field> getInjectableFields(Class<?> type) {
        return injectableFieldsCache.computeIfAbsent(type, this::findInjectableFields);
    }

    public List<Method> getPostConstructMethods(Class<?> type) {
        return postConstructMethodsCache.computeIfAbsent(type, this::findPostConstructMethods);
    }

    public List<Method> getPreDestroyMethods(Class<?> type) {
        return preDestroyMethodsCache.computeIfAbsent(type, this::findPreDestroyMethods);
    }

    private Constructor<?> findAutowireConstructor(Class<?> beanClass) {
        Constructor<?> autowireCtor = null;
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (autowireCtor != null) {
                    throw new DependencyInjectionException("Multiple @Inject constructors found for bean: " + beanClass.getName());
                }
                autowireCtor = constructor;
            }
        }
        if (autowireCtor == null) {
            try {
                autowireCtor = beanClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                // Não há construtor padrão nem @Inject. A falha real ocorrerá na instanciação se for necessário.
            }
        }
        if (autowireCtor != null) {
            autowireCtor.setAccessible(true);
        }
        return autowireCtor;
    }

    private List<Field> findInjectableFields(Class<?> beanClass) {
        return Arrays.stream(beanClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<Method> findPostConstructMethods(Class<?> beanClass) {
        return Arrays.stream(beanClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .peek(method -> method.setAccessible(true))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<Method> findPreDestroyMethods(Class<?> beanClass) {
        return Arrays.stream(beanClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PreDestroy.class))
                .peek(method -> method.setAccessible(true))
                .collect(Collectors.toUnmodifiableList());
    }
}
