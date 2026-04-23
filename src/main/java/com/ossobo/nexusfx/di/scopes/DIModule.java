package com.ossobo.nexusfx.di.scopes;


import com.ossobo.nexusfx.di.DiContainer;

/**
 * Interface para módulos de configuração do DIConteiner.
 * Permite estender as capacidades do contêiner de forma modular.
 */
public interface DIModule {
    /**
     * Configura o contêiner, registrando beans, custom scopes, etc.
     * Este método é chamado durante a fase de escaneamento de componentes.
     * @param context O contexto de registro de beans do DIConteiner.
     */
    void configure(BeanRegistry context);

    /**
     * Chamado após a inicialização completa de todos os singletons.
     * Pode ser usado para inicializações pós-construção ou para registrar listeners.
     * @param container A instância do DIConteiner.
     */
    default void onStart(DiContainer container) {}
}


