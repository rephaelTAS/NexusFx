/**
 * ===== StyleDefinition.java (MODIFICADO) =====
 *
 * MODIFICAÇÃO: Adicionar suporte a CSS classes no applyTo()
 * APENAS 1 MÉTODO MODIFICADO - O RESTO PERMANECE IGUAL
 */

package com.ossobo.nexusfx.view.design;

import com.ossobo.nexusfx.view.design.internal.CSSEngine;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class StyleDefinition {

    private final String cssClass;
    private final String customCssPath;
    private final CssMode mode;

    private StyleDefinition(Builder builder) {
        this.cssClass = builder.cssClass;
        this.customCssPath = builder.customCssPath;
        this.mode = builder.mode != null ? builder.mode : CssMode.AUTO;
    }

    // =========================================================================
    // 🎯 MÉTODO ÚNICO MODIFICADO - AGORA COM SUPORTE A CSS CLASSES
    // =========================================================================

    /**
     * ===== applyTo() - VERSÃO MODIFICADA =====
     *
     * ✅ AGORA PROCESSA CSS CLASSES DO FXML AUTOMATICAMENTE!
     * ✅ MANTÉM 100% COMPATIBILIDADE COM O FLUXO EXISTENTE
     *
     * @param node Nó a ser estilizado
     */
    public void applyTo(Node node) {
        if (node == null) return;

        // 1️⃣ CUSTOM CSS (arquivos .css do usuário) - NÃO MODIFICADO
        if (deveAplicarCustom() && hasCustomCss() && node instanceof Parent) {
            CSSEngine.getInstance().applyCss((Parent) node, customCssPath);
        }

        // 2️⃣ DESCOBRIR E APLICAR CSS CLASSES DO FXML
        //    ✅ NOVO: Processa TODAS as CSS classes do node
        if (deveAplicarDesignSystem() && node.getStyleClass() != null) {
            for (String cssClass : node.getStyleClass()) {
                if (cssClass != null && !cssClass.isEmpty()) {
                    // ✅ CHAMA O DESIGN SYSTEM PARA CADA CLASSE ENCONTRADA
                    DesignSystem.aplicarEstiloProgramado(node, cssClass);
                }
            }
        }

        // 3️⃣ CSS CLASS DO DESCRIPTOR (fallback/explícito)
        //    ✅ MANTIDO PARA COMPATIBILIDADE
        if (deveAplicarDesignSystem() && hasCssClass()) {
            DesignSystem.aplicarEstiloProgramado(node, this.cssClass);
        }
    }

    // ===== DECISÕES BASEADAS NO MODO =====
    private boolean deveAplicarCustom() {
        return mode == CssMode.AUTO || mode == CssMode.CUSTOM_ONLY;
    }

    private boolean deveAplicarDesignSystem() {
        return mode == CssMode.AUTO || mode == CssMode.DESIGN_SYSTEM_ONLY;
    }

    // ===== PREDICADOS =====
    public boolean hasCustomCss() {
        return customCssPath != null && !customCssPath.trim().isEmpty();
    }

    public boolean hasCssClass() {
        return cssClass != null && !cssClass.trim().isEmpty();
    }

    // ===== GETTER =====
    public CssMode getMode() { return mode; }

    // =========================================================================
    // 🏗️ BUILDER (COMPLETAMENTE IGUAL)
    // =========================================================================

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String cssClass;
        private String customCssPath;
        private CssMode mode;

        public Builder withCssClass(String cssClass) {
            this.cssClass = cssClass;
            return this;
        }

        public Builder withCustomCss(String customCssPath) {
            this.customCssPath = customCssPath;
            return this;
        }

        public Builder withMode(CssMode mode) {
            this.mode = mode;
            return this;
        }

        public StyleDefinition build() {
            return new StyleDefinition(this);
        }
    }

    // =========================================================================
    // 📌 ENUM - MODOS DE OPERAÇÃO (COMPLETAMENTE IGUAL)
    // =========================================================================

    public enum CssMode {
        /** Apenas CSS personalizado (.css do usuário) */
        CUSTOM_ONLY,

        /** Apenas CSS programado (DesignSystem Java) */
        DESIGN_SYSTEM_ONLY,

        /** Ambos: .css base + programado como sobrescrita */
        AUTO
    }
}
