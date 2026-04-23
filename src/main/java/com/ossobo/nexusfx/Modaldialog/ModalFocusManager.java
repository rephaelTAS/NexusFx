package com.ossobo.nexusfx.Modaldialog;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Gerencia foco entre múltiplos modais abertos
 */
public final class ModalFocusManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalFocusManager.class);
    private static final ModalFocusManager INSTANCE = new ModalFocusManager();
    private final List<Stage> openModals = new ArrayList<>();

    private ModalFocusManager() {
        LOGGER.info("✅ ModalFocusManager iniciado");
    }

    public static ModalFocusManager getInstance() { return INSTANCE; }

    public void registerModal(Stage modal) {
        openModals.add(modal);
        modal.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) bringToFront(modal);
        });
        bringToFront(modal);
        LOGGER.debug("📋 Modal registrado: {} (Total: {})",
                modal.getTitle(), openModals.size());
    }

    public void unregisterModal(Stage modal) {
        openModals.remove(modal);
        LOGGER.debug("📋 Modal removido: {} (Restantes: {})",
                modal.getTitle(), openModals.size());
    }

    public void bringToFront(Stage modal) {
        openModals.remove(modal);
        openModals.add(modal);
        modal.toFront();
        LOGGER.debug("⬆️ Modal trazido para frente: {}", modal.getTitle());
    }

    public void closeAll() {
        new ArrayList<>(openModals).forEach(Stage::close);
        openModals.clear();
    }

    public int getModalCount() { return openModals.size(); }
    public boolean hasOpenModals() { return !openModals.isEmpty(); }
}
