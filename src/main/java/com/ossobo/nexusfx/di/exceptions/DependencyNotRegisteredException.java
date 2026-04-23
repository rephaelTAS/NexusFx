package com.ossobo.nexusfx.di.exceptions;


public class DependencyNotRegisteredException extends RuntimeException {
    public DependencyNotRegisteredException(String message) {
        super(message);
    }
}