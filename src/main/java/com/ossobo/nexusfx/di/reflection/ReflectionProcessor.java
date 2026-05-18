package com.ossobo.nexusfx.di.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ReflectionProcessor v2.0
 *
 * Utilitário de baixo nível para manipulação segura de reflection.
 *
 * Responsabilidades:
 * - Injetar valores em campos (privados ou não)
 * - Invocar métodos (privados ou não)
 * - Instanciar classes via construtor (privado ou não)
 * - Garantir restauração do estado de acessibilidade
 *
 * Usado por InjectionManager e InstanceCreator.
 * Sem estado interno — thread-safe.
 *
 * @since 2.0
 */
public final class ReflectionProcessor {

    public ReflectionProcessor() {}

    // ===== INJEÇÃO DE CAMPO =====

    /**
     * Injeta um valor num campo específico de uma instância.
     * Trata campos privados e restaura o estado de acessibilidade.
     *
     * @param instance instância alvo
     * @param field    campo a injetar
     * @param value    valor a atribuir
     */
    public void injectField(Object instance, Field field, Object value) {
        boolean wasAccessible = field.canAccess(instance);

        try {
            if (!wasAccessible) {
                field.setAccessible(true);
            }
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Erro ao injetar campo '" + field.getName() +
                            "' em '" + instance.getClass().getName() + "': " + e.getMessage(), e);
        } finally {
            if (!wasAccessible) {
                field.setAccessible(false);
            }
        }
    }

    // ===== INVOCAÇÃO DE MÉTODO =====

    /**
     * Invoca um método numa instância com os argumentos fornecidos.
     * Trata métodos privados e restaura o estado de acessibilidade.
     *
     * @param instance instância alvo
     * @param method   método a invocar
     * @param args     argumentos do método
     * @return valor de retorno do método, ou null se void
     */
    public Object invokeMethod(Object instance, Method method, Object... args) {
        boolean wasAccessible = method.canAccess(instance);

        try {
            if (!wasAccessible) {
                method.setAccessible(true);
            }
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao invocar método '" + method.getName() +
                            "' em '" + instance.getClass().getName() + "': " + e.getMessage(), e);
        } finally {
            if (!wasAccessible) {
                method.setAccessible(false);
            }
        }
    }

    // ===== INSTANCIAÇÃO =====

    /**
     * Cria uma nova instância via construtor.
     * Trata construtores privados e restaura o estado de acessibilidade.
     *
     * @param constructor construtor a usar
     * @param args        argumentos do construtor
     * @param <T>         tipo da instância
     * @return nova instância
     */
    @SuppressWarnings("unchecked")
    public <T> T instantiate(Constructor<?> constructor, Object... args) {
        boolean wasAccessible = constructor.canAccess(null);

        try {
            if (!wasAccessible) {
                constructor.setAccessible(true);
            }
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao instanciar '" + constructor.getDeclaringClass().getName() +
                            "': " + e.getMessage(), e);
        } finally {
            if (!wasAccessible) {
                constructor.setAccessible(false);
            }
        }
    }

    // ===== LEITURA DE CAMPO =====

    /**
     * Lê o valor de um campo de uma instância.
     *
     * @param instance instância alvo
     * @param field    campo a ler
     * @return valor do campo
     */
    public Object readField(Object instance, Field field) {
        boolean wasAccessible = field.canAccess(instance);

        try {
            if (!wasAccessible) {
                field.setAccessible(true);
            }
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Erro ao ler campo '" + field.getName() +
                            "' de '" + instance.getClass().getName() + "': " + e.getMessage(), e);
        } finally {
            if (!wasAccessible) {
                field.setAccessible(false);
            }
        }
    }
}