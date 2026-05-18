package com.ossobo.nexusfx.di.exceptions;

/**
 * Exceção lançada quando ocorre um erro durante a execução
 * dos métodos de ciclo de vida do bean (@PostConstruct, @PreDestroy).
 */
public class LifecycleException extends RuntimeException {

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}