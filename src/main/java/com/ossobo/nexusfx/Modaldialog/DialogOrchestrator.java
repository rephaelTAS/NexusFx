package com.ossobo.nexusfx.Modaldialog;

import com.ossobo.nexusfx.di.DiContainer;
import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.Inject;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.scopes.ScopeType;
import com.ossobo.nexusfx.view.ViewManager;
import com.ossobo.nexusfx.view.views.LoadedView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@ScopeAnnotation(ScopeType.SINGLETON)
public final class DialogOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogOrchestrator.class);

    private final ViewManager viewManager;
    private final DialogStageFactory stageFactory;
    private final DialogControllerManager controllerManager;
    private final DialogResultExtractor resultExtractor;
    private final ModalStackManager modalStackManager;
    private final ModalFocusManager modalFocusManager; // ✅ NOVO

    @Inject
    public DialogOrchestrator(ViewManager viewManager) {
        this.viewManager = Objects.requireNonNull(viewManager);
        this.stageFactory = new DialogStageFactory();
        this.controllerManager = new DialogControllerManager();
        this.resultExtractor = new DialogResultExtractor();
        this.modalStackManager = ModalStackManager.getInstance();
        this.modalFocusManager = ModalFocusManager.getInstance(); // ✅ INICIALIZADO
        LOGGER.info("✅ DialogOrchestrator com foco manager");
    }

    // ==================== MÉTODOS CORRIGIDOS (SEM BLOQUEIO) ====================

    /**
     * ✅ ABRE MODAL SIMPLES (show() sem wait)
     */
    public void openModal(String viewId, String title, Window owner) {
        Objects.requireNonNull(viewId);
        Objects.requireNonNull(title);

        LOGGER.debug("🪟 Abrindo modal simples: {}", viewId);

        try {
            // ✅ SEM VERIFICAÇÃO DE BLOQUEIO
            LoadedView<?> loadedView = viewManager.loadFreshView(viewId);
            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ show() EM VEZ DE showAndWait()
            dialogStage.show();

            LOGGER.debug("✅ Modal simples aberto: {}", viewId);

        } catch (Exception e) {
            LOGGER.error("❌ Falha ao abrir modal: {}", viewId, e);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    /**
     * ✅ ABRE MODAL COM CONTROLLER (show() sem wait)
     */
    public <T> void openModalWithController(String viewId, String title, Window owner,
                                            Consumer<T> controllerConfigurator) {
        Objects.requireNonNull(viewId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(controllerConfigurator);

        LOGGER.debug("🪟 Abrindo modal com controller: {}", viewId);

        try {
            // ✅ SEM VERIFICAÇÃO DE BLOQUEIO
            LoadedView<?> loadedView = loadDialogView(viewId, controllerConfigurator);
            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ show() EM VEZ DE showAndWait()
            dialogStage.show();

            LOGGER.debug("✅ Modal com controller aberto: {}", viewId);

        } catch (Exception e) {
            LOGGER.error("❌ Falha ao abrir modal: {}", viewId, e);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    /**
     * ✅ ABRE MODAL COM TIPO ESPECÍFICO (show() sem wait)
     */
    public <T> void openModalWithController(String viewId, String title, Window owner,
                                            Class<T> controllerType,
                                            Consumer<T> controllerConfigurator) {
        Objects.requireNonNull(viewId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(controllerType);
        Objects.requireNonNull(controllerConfigurator);

        LOGGER.debug("🪟 Abrindo modal com tipo: {} [{}]", viewId, controllerType.getSimpleName());

        try {
            // ✅ SEM VERIFICAÇÃO DE BLOQUEIO
            LoadedView<?> loadedView = viewManager.loadFreshView(viewId, controllerType, controllerConfigurator);
            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ show() EM VEZ DE showAndWait()
            dialogStage.show();

            LOGGER.debug("✅ Modal com tipo específico aberto: {}", viewId);

        } catch (Exception e) {
            LOGGER.error("❌ Falha ao abrir modal: {}", viewId, e);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    /**
     * ✅ ABRE MODAL COM RETORNO (APENAS ESTE USA showAndWait())
     */
    public <T, R> Optional<R> openModalWithControllerWithResult(String viewId, String title, Window owner,
                                                                Consumer<T> controllerConfigurator,
                                                                Function<T, R> resultExtractor) {
        Objects.requireNonNull(viewId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(controllerConfigurator);
        Objects.requireNonNull(resultExtractor);

        LOGGER.debug("🪟 Abrindo modal com retorno: {}", viewId);

        LoadedView<?> loadedView = null;

        try {
            // ✅ SEM VERIFICAÇÃO DE BLOQUEIO
            loadedView = loadDialogView(viewId, controllerConfigurator);
            notifyViewLifecycle(loadedView, "shown");

            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ APENAS ESTE MANTÉM showAndWait() (para retorno)
            dialogStage.showAndWait();

            Optional<R> result = extractResultIfNeeded(loadedView, resultExtractor);
            LOGGER.debug("✅ Modal com retorno fechado: {}", viewId);
            return result;

        } catch (Exception e) {
            LOGGER.error("❌ Falha ao abrir modal: {}", viewId, e);
            cleanupOnError(loadedView);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    // ==================== MÉTODOS AUXILIARES CORRIGIDOS ====================

    /**
     * ✅ CRIA STAGE COM FOCO MANAGER
     */
    private Stage createDialogStageWithStack(Window owner, String title, LoadedView<?> loadedView) {
        try {
            Window correctOwner = getCorrectOwnerWithStack(owner);
            Stage stage = stageFactory.createDialogStage(correctOwner, title, loadedView.getRoot());

            // ✅ REGISTRAR NO FOCO MANAGER
            modalFocusManager.registerModal(stage);

            configureStackManagementV2(stage, title, correctOwner);

            if (loadedView.hasController()) {
                controllerManager.configureController(
                        loadedView.getController(),
                        null,
                        stage
                );
            }

            return stage;

        } catch (Exception e) {
            LOGGER.error("Falha ao criar stage: {}", e.getMessage());
            throw new DialogException("Falha ao criar stage", e);
        }
    }

    /**
     * ✅ CONFIGURA HANDLERS COM FOCO MANAGER
     */
    private void configureCloseHandlers(String viewId, LoadedView<?> loadedView, Stage dialogStage) {
        dialogStage.setOnHidden(e -> {
            try {
                notifyViewLifecycle(loadedView, "hidden");
                cleanupDialogView(viewId, loadedView);

                // ✅ REMOVER DO FOCO MANAGER
                modalFocusManager.unregisterModal(dialogStage);

                LOGGER.debug("Handlers executados para: {}", viewId);
            } catch (Exception ex) {
                LOGGER.warn("Erro nos handlers: {}", ex.getMessage());
            }
        });

        dialogStage.setOnCloseRequest(e -> {
            try {
                notifyViewLifecycle(loadedView, "hidden");
                cleanupDialogView(viewId, loadedView);
                modalFocusManager.unregisterModal(dialogStage);
            } catch (Exception ex) {
                LOGGER.debug("Erro no close request: {}", ex.getMessage());
            }
        });
    }

    /**
     * ✅ CONFIGURA GESTÃO DE PILHA SIMPLIFICADA
     */
    private void configureStackManagementV2(Stage stage, String title, Window owner) {
        modalStackManager.registerModalOpening(stage, owner);

        stage.setOnShown(e -> {
            modalStackManager.confirmModalOpened(stage);
            LOGGER.debug("✅ Modal '{}' visível", title);
        });

        stage.setOnHidden(e -> {
            modalStackManager.registerModalClosed(stage);
            LOGGER.debug("🔄 Modal '{}' fechado", title);
        });
    }

    // ==================== MÉTODOS LEGACY CORRIGIDOS ====================

    /**
     * ✅ EXIBE DIALOG SEM RETORNO (show() sem wait)
     */
    public <T> void showDialog(String viewId, Window owner, String title, Consumer<T> configurator) {
        try {
            LOGGER.debug("Iniciando dialog: {}", viewId);

            LoadedView<?> loadedView = loadDialogView(viewId, configurator);
            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ show() EM VEZ DE showAndWait()
            dialogStage.show();

            LOGGER.debug("✅ Dialog aberto: {}", viewId);

        } catch (Exception e) {
            LOGGER.error("❌ Falha: {}", viewId, e);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    /**
     * ✅ EXIBE DIALOG COM RETORNO (showAndWait() mantido)
     */
    public <T, R> Optional<R> showDialogForResult(String viewId, Window owner, String title,
                                                  Consumer<T> configurator, Function<T, R> resultExtractor) {
        LoadedView<?> loadedView = null;

        try {
            LOGGER.debug("Iniciando dialog com retorno: {}", viewId);

            loadedView = loadDialogView(viewId, configurator);
            notifyViewLifecycle(loadedView, "shown");

            Stage dialogStage = createDialogStageWithStack(owner, title, loadedView);
            configureCloseHandlers(viewId, loadedView, dialogStage);

            // ✅ MANTÉM showAndWait() PARA RETORNO
            dialogStage.showAndWait();

            Optional<R> result = extractResultIfNeeded(loadedView, resultExtractor);
            LOGGER.debug("✅ Dialog com retorno finalizado: {}", viewId);
            return result;

        } catch (Exception e) {
            LOGGER.error("❌ Falha: {}", viewId, e);
            cleanupOnError(loadedView);
            throw new DialogException("Falha: " + viewId, e);
        }
    }

    // ==================== MÉTODOS AUXILIARES (MANTIDOS) ====================

    @SuppressWarnings("unchecked")
    private <T> LoadedView<?> loadDialogView(String viewId, Consumer<T> configurator) {
        try {
            if (configurator != null) {
                Consumer<Object> config = obj -> {
                    try {
                        configurator.accept((T) obj);
                    } catch (ClassCastException e) {
                        LOGGER.warn("Configurator tipo incompatível: {}", e.getMessage());
                    }
                };
                return viewManager.loadFreshView(viewId, config);
            } else {
                return viewManager.loadFreshView(viewId);
            }
        } catch (Exception e) {
            LOGGER.error("Falha ao carregar view: {}", viewId, e);
            throw new DialogException("Falha ao carregar view: " + viewId, e);
        }
    }

    private Window getCorrectOwnerWithStack(Window requestedOwner) {
        if (modalStackManager.hasActiveModals()) {
            Window topModal = modalStackManager.getTopModalWindow();
            if (topModal instanceof Stage && ((Stage) topModal).isShowing()) {
                return topModal;
            }
        }

        if (requestedOwner instanceof Stage) {
            Stage requestedStage = (Stage) requestedOwner;
            if (requestedStage.isShowing() && modalStackManager.isActiveModal(requestedStage)) {
                return requestedOwner;
            }
        }

        return requestedOwner;
    }

    private void cleanupDialogView(String viewId, LoadedView<?> loadedView) {
        try {
            viewManager.detachViewFromScene(viewId, loadedView.getRoot());
            loadedView.detachFromScene();
            if (loadedView.isRefreshable()) {
                loadedView.notifyViewHidden();
            }
        } catch (Exception e) {
            LOGGER.warn("Erro ao limpar view: {}", e.getMessage());
        }
    }

    private void notifyViewLifecycle(LoadedView<?> loadedView, String event) {
        try {
            if ("shown".equals(event)) {
                loadedView.notifyViewShown();
                if (loadedView.isRefreshable()) {
                    loadedView.refreshIfNeeded();
                }
            } else if ("hidden".equals(event)) {
                loadedView.notifyViewHidden();
            }
        } catch (Exception e) {
            LOGGER.warn("Erro ao notificar ciclo: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T, R> Optional<R> extractResultIfNeeded(LoadedView<?> loadedView,
                                                     Function<T, R> resultExtractor) {
        if (resultExtractor != null && loadedView.hasController()) {
            try {
                T controller = (T) loadedView.getController();
                return this.resultExtractor.extractResult(controller, resultExtractor);
            } catch (ClassCastException e) {
                LOGGER.error("Erro de cast: {}", e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private void cleanupOnError(LoadedView<?> loadedView) {
        if (loadedView != null) {
            try {
                loadedView.detachFromScene();
            } catch (Exception e) {
                LOGGER.warn("Erro durante cleanup: {}", e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS PÚBLICOS ADICIONAIS ====================

    public void forceCloseAllModals() {
        LOGGER.warn("Forçando fechamento de todos os modais");
        modalStackManager.clearStack();
        modalFocusManager.closeAll();
    }

    public boolean hasActiveModal() {
        return modalStackManager.hasActiveModals();
    }

    public int getActiveModalCount() {
        return modalStackManager.getStackSize();
    }

    public boolean isActiveModal(Stage stage) {
        return modalStackManager.isActiveModal(stage);
    }

    // ==================== CLASSES AUXILIARES ====================

    public static class DialogOptions {
        private double width;
        private double height;
        private Boolean resizable;
        private double minWidth;
        private double minHeight;

        public DialogOptions width(double width) { this.width = width; return this; }
        public DialogOptions height(double height) { this.height = height; return this; }
        public DialogOptions resizable(boolean resizable) { this.resizable = resizable; return this; }
        public DialogOptions minWidth(double minWidth) { this.minWidth = minWidth; return this; }
        public DialogOptions minHeight(double minHeight) { this.minHeight = minHeight; return this; }

        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public Boolean isResizable() { return resizable; }
        public double getMinWidth() { return minWidth; }
        public double getMinHeight() { return minHeight; }
    }

    public static class DialogException extends RuntimeException {
        public DialogException(String message) { super(message); }
        public DialogException(String message, Throwable cause) { super(message, cause); }
    }

    // ==================== MÉTODOS ESTÁTICOS ====================

    public static DialogOrchestrator getInstance() {
        try {
            return DiContainer.getInstance()
                    .getBean(DialogOrchestrator.class);
        } catch (Exception e) {
            throw new DialogException("DialogOrchestrator não disponível", e);
        }
    }

    public static void open(String viewId, String title, Window owner) {
        getInstance().openModal(viewId, title, owner);
    }

    public static <T> void openWithController(String viewId, String title, Window owner,
                                              Consumer<T> controllerConfigurator) {
        getInstance().openModalWithController(viewId, title, owner, controllerConfigurator);
    }

    public static <T> void openWithController(String viewId, String title, Window owner,
                                              Class<T> controllerType,
                                              Consumer<T> controllerConfigurator) {
        getInstance().openModalWithController(viewId, title, owner, controllerType, controllerConfigurator);
    }

    public static <T, R> Optional<R> openWithControllerAndResult(String viewId, String title, Window owner,
                                                                 Consumer<T> controllerConfigurator,
                                                                 Function<T, R> resultExtractor) {
        return getInstance().openModalWithControllerWithResult(viewId, title, owner,
                controllerConfigurator, resultExtractor);
    }

    public static <T> void show(String viewId, Window owner, String title, Consumer<T> configurator) {
        getInstance().showDialog(viewId, owner, title, configurator);
    }

    public static <T, R> Optional<R> showForResult(String viewId, Window owner, String title,
                                                   Consumer<T> configurator, Function<T, R> resultExtractor) {
        return getInstance().showDialogForResult(viewId, owner, title, configurator, resultExtractor);
    }
}
