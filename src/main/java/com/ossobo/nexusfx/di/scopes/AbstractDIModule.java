package com.ossobo.nexusfx.di.scopes;

abstract class AbstractDIModule implements DIModule { // Acesso default
    protected BeanRegistry registry;

    @Override
    public void configure(BeanRegistry registry) {
        this.registry = registry;
        doConfigure(); // Chama o método abstrato que subclasses devem implementar
    }

    /**
     * Método a ser implementado por subclasses para configurar seus beans.
     */
    protected abstract void doConfigure();
}