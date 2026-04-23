package com.ossobo.nexusfx.di.scopes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingletonScope implements ScopeHandler {
    private static final Logger LOGGER = Logger.getLogger(SingletonScope.class.getName());

    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> earlySingletonObjects = new ConcurrentHashMap<>(); // Não usado diretamente agora, mas parte do fluxo robusto
    private final Map<Class<?>, Boolean> singletonsCurrentlyInCreation = new ConcurrentHashMap<>();


    @Override
    public <T> T get(Class<T> type, Supplier<T> supplier) {
        // 1. Verifica se a instância já existe e está completamente inicializada
        Object singletonObject = singletons.get(type);
        if (singletonObject != null) {
            LOGGER.log(Level.FINE, "SingletonScope: Instância de {0} já existe e está pronta.", type.getName());
            return type.cast(singletonObject);
        }

        // 2. Verifica se a instância está atualmente em criação (dependência circular)
        if (singletonsCurrentlyInCreation.containsKey(type)) {
            LOGGER.log(Level.SEVERE, "SingletonScope: Dependência circular detectada para o tipo {0}. " +
                    "A instância já está em processo de criação. Isso indica um ciclo de injeção no construtor.", type.getName());
            throw new IllegalStateException("Dependência circular detectada para " + type.getName() +
                    ". Verifique as dependências do construtor.");
        }

        // 3. Verifica se existe uma referência "early" (preliminar) da instância (para ciclos de setter/field)
        Object earlySingleton = earlySingletonObjects.get(type);
        if (earlySingleton != null) {
            LOGGER.log(Level.FINE, "SingletonScope: Retornando referência 'early' para {0}.", type.getName());
            return type.cast(earlySingleton);
        }

        // 4. Se a instância não existe e não está em criação, inicia a criação.
        singletonsCurrentlyInCreation.put(type, Boolean.TRUE); // Marca como em criação
        LOGGER.log(Level.INFO, "SingletonScope: Iniciando criação da instância para {0}.", type.getName());

        try {
            // Cria a instância usando o supplier.
            T newInstance = supplier.get();

            // Depois que a instância é criada, remove-a de "em criação"
            // e a adiciona ao mapa de singletons completos.
            singletonsCurrentlyInCreation.remove(type);
            singletons.put(type, newInstance); // Adiciona ao mapa de singletons completos

            LOGGER.log(Level.INFO, "SingletonScope: Instância de {0} criada e registrada como singleton completo.", type.getName());
            return newInstance;

        } catch (Exception e) {
            // Se houver qualquer exceção durante a criação, remove do estado de criação
            singletonsCurrentlyInCreation.remove(type);
            // CORREÇÃO AQUI: Passa a exceção como o último argumento
            LOGGER.log(Level.SEVERE, "SingletonScope: Falha ao criar instância de " + type.getName() + ".", e);
            throw new RuntimeException("Falha ao criar instância de " + type.getName(), e);
        }
    }

    @Override
    public void remove(Class<?> type) {
        singletons.remove(type);
        earlySingletonObjects.remove(type);
        singletonsCurrentlyInCreation.remove(type);
        LOGGER.log(Level.FINE, "SingletonScope: Instância de {0} removida (se existente).", type.getName());
    }

    @Override
    public void clear() {
        singletons.clear();
        earlySingletonObjects.clear();
        singletonsCurrentlyInCreation.clear();
        LOGGER.log(Level.FINE, "SingletonScope limpo. Todas as instâncias singleton foram removidas.");
    }

    public Collection<Object> getAllManagedInstances() {
        return singletons.values();
    }

    public Map<Class<?>, Object> getAllInstances() {
        return Collections.unmodifiableMap(singletons);
    }
}