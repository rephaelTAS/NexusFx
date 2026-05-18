package com.ossobo.nexusfx.di.exceptions;

public class DependencyNotRegisteredException extends RuntimeException {
    public DependencyNotRegisteredException(String beanNameOrType) {
        super("Dependência não registrada: Nenhum bean do tipo '" + beanNameOrType + "' foi encontrado no container.");
    }
}