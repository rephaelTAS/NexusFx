package com.ossobo.nexusfx.di.scopes;

public enum ScopeType {
    SINGLETON("singleton"),
    THREAD("thread"),
    PROTOTYPE("prototype"); // Exemplo, adicione outros se precisar

    private final String name;

    ScopeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}