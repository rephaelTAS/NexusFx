package com.ossobo.nexusfx.di.scopes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// Manipulador para o escopo Thread.
// Garante que uma instância de uma dependência seja única por thread que a solicita.
public class ThreadScope implements ScopeHandler {
    // ThreadLocal para armazenar um mapa de instâncias para cada thread.
    // Cada thread terá seu próprio mapa de instâncias, garantindo isolamento.
    private final ThreadLocal<Map<Class<?>, Object>> threadInstances =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * Obtém uma instância do tipo especificado no escopo de thread.
     * A instância é única para a thread atual. Se não existir, é criada e armazenada no ThreadLocal da thread.
     * @param type O tipo da instância a ser obtida.
     * @param creator O Supplier para criar a instância se ela ainda não existir nesta thread.
     * @param <T> O tipo da instância.
     * @return A instância do tipo especificado para a thread atual.
     */
    @Override
    public <T> T get(Class<T> type, Supplier<T> creator) {
        // Obtém o mapa de instâncias para a thread atual e usa computeIfAbsent.
        return type.cast(threadInstances.get().computeIfAbsent(type, k -> creator.get()));
    }

    /**
     * Remove uma instância específica do escopo da thread atual.
     * @param type O tipo da instância a ser removida.
     */
    @Override
    public void remove(Class<?> type) {
        threadInstances.get().remove(type);
    }

    /**
     * Limpa todas as instâncias gerenciadas pelo escopo da thread atual.
     * Este método remove o mapa de instâncias do ThreadLocal para a thread em questão.
     */
    @Override
    public void clear() {
        threadInstances.remove(); // Remove o mapa completo da thread atual do ThreadLocal
    }
}