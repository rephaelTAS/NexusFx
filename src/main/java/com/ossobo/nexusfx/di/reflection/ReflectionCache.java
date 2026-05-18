package com.ossobo.nexusfx.di.reflection;

import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.di.annotations.PostConstruct;
import com.ossobo.nexusfx.di.annotations.PreDestroy;
import com.ossobo.nexusfx.di.scanner.ReflectionScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReflectionCache v2.0
 *
 * Cache filtrado de metadados de reflection específicos do framework.
 *
 * Responsabilidade: armazenar métodos e campos anotados com
 * @Inject, @PostConstruct, @PreDestroy, e o construtor injetável.
 *
 * Usa ConcurrentHashMap direto (sem SoftReference) porque estes
 * metadados são essenciais e não devem ser descartados pelo GC.
 *
 * Thread-safe. Métricas de hits/misses.
 *
 * @since 2.0
 */
public final class ReflectionCache {

    private final Map<Class<?>, Constructor<?>> injectableConstructors = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> injectableFields = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> injectableMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> postConstructMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> preDestroyMethods = new ConcurrentHashMap<>();

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    private final ReflectionScanner scanner;

    public ReflectionCache(ReflectionScanner scanner) {
        this.scanner = scanner;
    }

    // ===== CONSTRUTOR INJETÁVEL =====

    public Constructor<?> getInjectableConstructor(Class<?> type) {
        return injectableConstructors.computeIfAbsent(type, t -> {
            List<Constructor<?>> ctors = scanner.getConstructors(t);
            // @Inject explícito
            for (Constructor<?> c : ctors) {
                if (c.isAnnotationPresent(Inject.class)) return c;
            }
            // Construtor padrão
            try { return t.getDeclaredConstructor(); } catch (NoSuchMethodException e) {
                // Único construtor
                if (ctors.size() == 1) return ctors.get(0);
                throw new RuntimeException("Nenhum construtor adequado: " + t.getName());
            }
        });
    }

    // ===== @Inject FIELDS =====

    public List<Field> getInjectableFields(Class<?> type) {
        return cache(injectableFields, type,
                () -> scanner.getFieldsWithAnnotation(type, Inject.class));
    }

    // ===== @Inject METHODS =====

    public List<Method> getInjectableMethods(Class<?> type) {
        return cache(injectableMethods, type, () -> {
            List<Method> methods = scanner.getMethodsWithAnnotation(type, Inject.class);
            return methods.stream()
                    .filter(m -> m.getParameterCount() > 0)
                    .toList();
        });
    }

    // ===== @PostConstruct =====

    public List<Method> getPostConstructMethods(Class<?> type) {
        return cache(postConstructMethods, type, () -> {
            List<Method> methods = scanner.getMethodsWithAnnotation(type, PostConstruct.class);
            return methods.stream()
                    .filter(m -> m.getParameterCount() == 0)
                    .toList();
        });
    }

    // ===== @PreDestroy =====

    public List<Method> getPreDestroyMethods(Class<?> type) {
        return cache(preDestroyMethods, type, () -> {
            List<Method> methods = scanner.getMethodsWithAnnotation(type, PreDestroy.class);
            return methods.stream()
                    .filter(m -> m.getParameterCount() == 0)
                    .toList();
        });
    }

    // ===== MÉTRICAS =====

    public long getHits() { return hits.get(); }
    public long getMisses() { return misses.get(); }

    public Map<String, Long> getStatistics() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("hits", h);
        stats.put("misses", m);
        stats.put("total", total);
        stats.put("hitRatePercent", total == 0 ? 0 : (h * 100 / total));
        return stats;
    }

    // ===== LIMPEZA =====

    public void clear() {
        injectableConstructors.clear();
        injectableFields.clear();
        injectableMethods.clear();
        postConstructMethods.clear();
        preDestroyMethods.clear();
        hits.set(0);
        misses.set(0);
    }

    // ===== INTERNO =====

    private <T> T cache(Map<Class<?>, T> map, Class<?> key, java.util.function.Supplier<T> loader) {
        if (map.containsKey(key)) {
            hits.incrementAndGet();
            return map.get(key);
        }
        misses.incrementAndGet();
        T value = loader.get();
        map.put(key, value);
        return value;
    }
}