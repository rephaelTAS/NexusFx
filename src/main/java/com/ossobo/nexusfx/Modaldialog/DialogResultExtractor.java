package com.ossobo.nexusfx.Modaldialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

/**
 * ✅ UMA RESPONSABILIDADE: Extrair resultados de dialogs
 * Staff Engineer Principle: Extração especializada e type-safe
 */
public final class DialogResultExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogResultExtractor.class);

    /**
     * ✅ EXTRAI RESULTADO DE CONTROLLER
     */
    public <T, R> Optional<R> extractResult(T controller, Function<T, R> resultExtractor) {
        if (controller == null || resultExtractor == null) {
            return Optional.empty();
        }

        try {
            R result = resultExtractor.apply(controller);
            LOGGER.debug("Resultado extraído do controller: {}",
                    controller.getClass().getSimpleName());
            return Optional.ofNullable(result);

        } catch (Exception e) {
            LOGGER.warn("Falha ao extrair resultado do controller", e);
            return Optional.empty();
        }
    }

    /**
     * ✅ EXTRAI RESULTADO BOOLEANO (para confirmações)
     */
    public Optional<Boolean> extractBooleanResult(Object controller, Function<Object, Boolean> extractor) {
        return extractResult(controller, extractor);
    }
}
