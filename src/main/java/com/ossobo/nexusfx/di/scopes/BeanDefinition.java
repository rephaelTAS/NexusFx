package com.ossobo.nexusfx.di.scopes;

class BeanDefinition { // Acesso default, pois é interna ao pacote principal do DIContainer
    private final Class<?> beanClass;
    private final String name;
    private final ScopeType scopeType;

    public BeanDefinition(Class<?> beanClass, String name, ScopeType scopeType) {
        this.beanClass = beanClass;
        this.name = name;
        this.scopeType = scopeType;
    }

    public Class<?> getBeanClass() { return beanClass; }
    public String getName() { return name; }
    public ScopeType getScopeType() { return scopeType; }
}