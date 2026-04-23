package com.ossobo.nexusfx.Modaldialog;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * ✅ Gerenciador de Pilha de Modals - VERSÃO CORRIGIDA
 * ✅ SEM bloqueio global, SEM limite artificial
 * ✅ PERMITE múltiplos modais concorrentes
 */
public final class ModalStackManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalStackManager.class);
    private static final ModalStackManager INSTANCE = new ModalStackManager();

    // ✅ PILHA DE CONTEXTO SIMPLIFICADA
    private final Deque<ModalContext> contextStack = new ArrayDeque<>();

    /**
     * ✅ CONTEXTO DE MODAL (simplificado)
     */
    private static class ModalContext {
        final Stage modalStage;
        final Window ownerWhenOpened;

        ModalContext(Stage modalStage, Window ownerWhenOpened) {
            this.modalStage = modalStage;
            this.ownerWhenOpened = ownerWhenOpened;
        }

        @Override
        public String toString() {
            return String.format("Modal[title='%s']", modalStage.getTitle());
        }
    }

    private ModalStackManager() {
        LOGGER.info("✅ ModalStackManager iniciado - Modo Sem Bloqueio");
    }

    public static ModalStackManager getInstance() {
        return INSTANCE;
    }

    // ==================== MÉTODOS CORRIGIDOS ====================

    /**
     * ✅ VERIFICA SE PODE ABRIR NOVO MODAL (SEM BLOQUEIO)
     * SEMPRE retorna true - permite múltiplos modais
     */
    public boolean canOpenNewModal() {
        synchronized (contextStack) {
            // ✅ SEM LIMITE, SEM BLOQUEIO
            boolean canOpen = true;
            LOGGER.debug("🔍 Pode abrir novo modal? {} (Stack size: {})",
                    canOpen, contextStack.size());
            return canOpen;
        }
    }

    /**
     * ✅ ADICIONA MODAL À PILHA (SEM modalInProgress)
     */
    public void pushModal(Stage modalStage) {
        synchronized (contextStack) {
            Window owner = modalStage.getOwner();
            ModalContext context = new ModalContext(modalStage, owner);
            contextStack.push(context);

            LOGGER.debug("📥 Modal empilhado: {} (Total: {})",
                    modalStage.getTitle(), contextStack.size());
        }
    }

    /**
     * ✅ REMOVE MODAL DA PILHA (SEM modalInProgress)
     */
    public void popModal(Stage modalStage) {
        synchronized (contextStack) {
            if (!contextStack.isEmpty() && contextStack.peek().modalStage == modalStage) {
                contextStack.pop();
                LOGGER.debug("📤 Modal desempilhado: {} (Total: {})",
                        modalStage.getTitle(), contextStack.size());
            } else {
                LOGGER.warn("⚠️ Tentativa de desempilhar modal não no topo");
                // Limpeza emergencial
                contextStack.removeIf(ctx -> ctx.modalStage == modalStage);
            }
        }
    }

    /**
     * ✅ OBTÉM JANELA DO TOPO
     */
    public Window getTopModalWindow() {
        synchronized (contextStack) {
            if (contextStack.isEmpty()) {
                LOGGER.debug("🎯 Nenhum modal ativo - topo: null");
                return null;
            }

            ModalContext topContext = contextStack.peek();
            return topContext.modalStage;
        }
    }

    /**
     * ✅ VERIFICA SE É MODAL ATIVO
     */
    public boolean isActiveModal(Stage stage) {
        synchronized (contextStack) {
            boolean isActive = !contextStack.isEmpty() && contextStack.peek().modalStage == stage;
            LOGGER.debug("🔍 Stage '{}' é modal ativo? {}", stage.getTitle(), isActive);
            return isActive;
        }
    }

    /**
     * ✅ TEM MODAIS ATIVOS?
     */
    public boolean hasActiveModals() {
        synchronized (contextStack) {
            boolean hasModals = !contextStack.isEmpty();
            LOGGER.debug("🔍 Tem modais ativos? {}", hasModals);
            return hasModals;
        }
    }

    /**
     * ✅ TAMANHO DA PILHA
     */
    public int getStackSize() {
        synchronized (contextStack) {
            return contextStack.size();
        }
    }

    /**
     * ✅ LIMPA PILHA
     */
    public void clearStack() {
        synchronized (contextStack) {
            LOGGER.warn("🚨 Limpando pilha de {} modais", contextStack.size());
            contextStack.clear();
        }
    }

    // ==================== MÉTODOS DA NOVA LÓGICA ====================

    /**
     * ✅ REGISTRA ABERTURA DE MODAL
     */
    public void registerModalOpening(Stage modalStage, Window effectiveOwner) {
        pushModal(modalStage);
        LOGGER.debug("📝 Modal registrado para abertura: {}", modalStage.getTitle());
    }

    /**
     * ✅ CONFIRMA MODAL ABERTO
     */
    public void confirmModalOpened(Stage modalStage) {
        LOGGER.debug("✅ Modal confirmado como aberto: {}", modalStage.getTitle());
    }

    /**
     * ✅ REGISTRA FECHAMENTO DE MODAL
     */
    public void registerModalClosed(Stage modalStage) {
        popModal(modalStage);
        LOGGER.debug("📝 Fechamento registrado: {}", modalStage.getTitle());
    }

    /**
     * ✅ OBTER CONTEXTO PARA DEBUG
     */
    public String getStackDebugInfo() {
        synchronized (contextStack) {
            if (contextStack.isEmpty()) {
                return "[]";
            }

            StringBuilder sb = new StringBuilder("[");
            for (ModalContext ctx : contextStack) {
                String title = ctx.modalStage.getTitle();
                sb.append(title != null ? title : "Sem título").append(" -> ");
            }
            sb.append("END]");
            return sb.toString();
        }
    }
}
