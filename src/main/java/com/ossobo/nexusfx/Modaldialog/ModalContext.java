package com.ossobo.nexusfx.Modaldialog;

import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.atomic.AtomicBoolean;

// ==== ModalContext com estado individual ====
public class ModalContext {
    final Stage modalStage;
    final Window ownerWhenOpened;
    final AtomicBoolean isOpening = new AtomicBoolean(false); // ⚠️ INDIVIDUAL

    ModalContext(Stage modalStage, Window ownerWhenOpened) {
        this.modalStage = modalStage;
        this.ownerWhenOpened = ownerWhenOpened;
    }

    // ✅ Verifica se ESTE modal específico pode ser aberto
    public boolean canOpen() {
        return !isOpening.getAndSet(true);
    }

    // ✅ Marca como aberto
    public void markAsOpened() {
        isOpening.set(false); // Já abriu, pode abrir outro
    }
}
