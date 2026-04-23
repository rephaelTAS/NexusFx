package com.ossobo.nexusfx.di.exceptions;

public class CircularDependencyException extends DependencyInjectionException {
    public CircularDependencyException(String message) {
        super(message);
    }
}