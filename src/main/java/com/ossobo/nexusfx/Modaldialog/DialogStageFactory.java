package com.ossobo.nexusfx.Modaldialog;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * ✅ Factory com modalidade inteligente
 */
public final class DialogStageFactory {
    private static final ModalStackManager modalStack = ModalStackManager.getInstance();

    public Stage createDialogStage(Window owner, String title, Parent root) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title != null ? title : "");

        // ✅ Modalidade adaptativa
        if (modalStack.hasActiveModals()) {
            dialogStage.initModality(Modality.WINDOW_MODAL);
        } else {
            dialogStage.initModality(Modality.APPLICATION_MODAL);
        }

        if (owner != null) dialogStage.initOwner(owner);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.setResizable(true);
        dialogStage.centerOnScreen();

        return dialogStage;
    }
}
