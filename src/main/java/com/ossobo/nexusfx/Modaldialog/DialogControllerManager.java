package com.ossobo.nexusfx.Modaldialog;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * ✅ UMA RESPONSABILIDADE: Gerenciar ciclo de vida de controllers de dialog
 * Staff Engineer Principle: Gestão especializada de controllers
 */
public final class DialogControllerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogControllerManager.class);

    /**
     * ✅ CONFIGURA CONTROLLER ANTES DE EXIBIR
     */
    public <T> void configureController(T controller, Consumer<T> configurator, Stage dialogStage) {
        Objects.requireNonNull(controller, "Controller não pode ser nulo");

        // ✅ CONFIGURAÇÃO PERSONALIZADA
        if (configurator != null) {
            configurator.accept(controller);
        }

        // ✅ CONFIGURAÇÃO DE STAGE PARA CONTROLLERS CLOSEABLE
        if (controller instanceof CloseableController) {
            ((CloseableController) controller).setDialogStage(dialogStage);
        }

        LOGGER.debug("Controller configurado: {}", controller.getClass().getSimpleName());
    }

    /**
     * ✅ INTERFACE PARA CONTROLLERS QUE FECHAM DIALOGS
     */
    public interface CloseableController {
        void setDialogStage(Stage stage);

        default void closeDialog() {
            // ✅ IMPLEMENTAÇÃO PADRÃO VAZIA - controllers implementam se necessário
        }
    }

    /**
     * ✅ VALIDA CONTROLLER COMPATÍVEL
     */
    public boolean isControllerValid(Object controller) {
        return controller != null;
    }
}
