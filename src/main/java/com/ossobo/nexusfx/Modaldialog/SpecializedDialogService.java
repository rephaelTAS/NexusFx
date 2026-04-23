package com.ossobo.nexusfx.Modaldialog;

import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * ✅ UMA RESPONSABILIDADE: Fornecer dialogs pré-configurados especializados
 * Staff Engineer Principle: Especialização em dialogs comuns
 */
public final class SpecializedDialogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecializedDialogService.class);

    private final DialogOrchestrator dialogOrchestrator;

    public SpecializedDialogService(DialogOrchestrator dialogOrchestrator) {
        this.dialogOrchestrator = dialogOrchestrator;
    }

    /**
     * ✅ EXIBE DIALOG DE CONFIRMAÇÃO
     */
    public boolean showConfirmationDialog(Window owner, String title, String message) {
        LOGGER.debug("Exibindo dialog de confirmação: {}", title);

        // ✅ USA ORQUESTRADOR PARA CARREGAMENTO E EXIBIÇÃO
        Optional<Boolean> result = dialogOrchestrator.showDialogForResult(
                "confirmation_dialog",
                owner,
                title,
                controller -> configureConfirmationController(controller, message),
                controller -> extractConfirmationResult(controller)
        );

        return result.orElse(false);
    }

    /**
     * ✅ EXIBE DIALOG DE ALERTA
     */
    public void showAlertDialog(Window owner, String title, String message) {
        LOGGER.debug("Exibindo dialog de alerta: {}", title);

        dialogOrchestrator.showDialog(
                "alert_dialog",
                owner,
                title,
                controller -> configureAlertController(controller, message)
        );
    }

    /**
     * ✅ EXIBE DIALOG DE ERRO
     */
    public void showErrorDialog(Window owner, String title, String errorMessage) {
        LOGGER.debug("Exibindo dialog de erro: {}", title);

        dialogOrchestrator.showDialog(
                "error_dialog",
                owner,
                title,
                controller -> configureErrorController(controller, errorMessage)
        );
    }

    // ✅ MÉTODOS DE CONFIGURAÇÃO ESPECIALIZADOS
    private void configureConfirmationController(Object controller, String message) {
        // Implementação específica para controller de confirmação
    }

    private void configureAlertController(Object controller, String message) {
        // Implementação específica para controller de alerta
    }

    private void configureErrorController(Object controller, String errorMessage) {
        // Implementação específica para controller de erro
    }

    private Boolean extractConfirmationResult(Object controller) {
        // Implementação específica para extrair resultado de confirmação
        return false; // Placeholder
    }
}
