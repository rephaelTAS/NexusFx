/**
 * ===== DesignSystem.java =====
 * PACOTE: com.ossobo.nexusfx.ViewEngine.design
 *
 * RESPONSABILIDADE ÚNICA:
 * APENAS CSS programado Java via switch fixo.
 *
 * =========================================================================
 * 📌 HISTÓRICO DE VERSÕES
 * =========================================================================
 *
 * v1.0 (12/02/2026) - Versão inicial
 *      - Switch fixo com casos para cards, botões, containers, textos
 *      - Método hasStyle() com sintaxe INCORRETA (erro de compilação)
 *      ⚠️ PROBLEMA: múltiplos 'case' dentro da mesma expressão switch
 *
 * v1.1 (12/02/2026) - CORREÇÃO DE SINTAXE
 *      ✅ hasStyle(): switch expression com sintaxe CORRETA
 *      ✅ Cada grupo de CSS classes é um ÚNICO case com múltiplos labels
 *      ✅ Compilação 100% garantida
 *      🔥 DECISÃO: Manter switch fixo, SEM mapa, SEM registro
 *
 * v1.2 (12/02/2026) - INTEGRAÇÃO COM NEUMORPHIC SYSTEM
 *      ✅ DELEGAÇÃO TOTAL para NeumorphicDesignSystemIntegrator
 *      ✅ IMPLEMENTAÇÃO COMPLETA dos estilos neumorphic
 *      ✅ 100% Java, sem CSS, sem duplicação de código
 *      🔥 DECISÃO: DesignSystem vira fachada (facade) pura
 *
 * =========================================================================
 * 📍 ESTADO ATUAL: v1.2 (✅ IMPLEMENTADA - INTEGRAÇÃO TOTAL)
 * =========================================================================
 */
package com.ossobo.nexusfx.view.design;

// ===== IMPORTAÇÃO ÚNICA - TODO O SISTEMA NEUMORPHIC =====
import com.ossobo.nexusfx.view.design.themes.neumorphic.NeumorphicUISystem;
import javafx.scene.Node;

public final class DesignSystem {

    private static final DesignSystem INSTANCE = new DesignSystem();

    private DesignSystem() {}

    public static DesignSystem getInstance() {
        return INSTANCE;
    }

    // =========================================================================
    // 🎯 MÉTODO PRINCIPAL - APLICAR ESTILO PROGRAMADO
    // =========================================================================
    // ✅ v1.2: DELEGAÇÃO TOTAL para o NeumorphicDesignSystemIntegrator
    // ✅ Mantém a mesma assinatura e comportamento
    // ✅ Todo código de estilo foi movido para o integrador
    //
    // NOTA: Seus appliers antigos (NeumorphicCardApplier, etc)
    //       NÃO SÃO MAIS NECESSÁRIOS - FORAM SUBSTITUÍDOS
    // =========================================================================

    /**
     * Aplica estilo programado JavaFX baseado na CSS class do nó.
     * Chamado APENAS por StyleDefinition.applyTo().
     *
     * 🔧 v1.2: Implementação delegada ao Neumorphic System
     *
     * @param node Nó a ser estilizado
     * @param cssClass Classe CSS que define o estilo
     */
    public static void aplicarEstiloProgramado(Node node, String cssClass) {
        if (node == null || cssClass == null || cssClass.isEmpty()) {
            return;
        }

        // ✅ ÚNICA LINHA - DELEGAÇÃO COMPLETA
        NeumorphicUISystem.aplicar(node, cssClass);
    }

    // =========================================================================
    // 🔍 UTILITÁRIO - VERIFICAR SE CLASSE POSSUI ESTILO
    // =========================================================================
    // ✅ v1.2: DELEGAÇÃO TOTAL para o NeumorphicDesignSystemIntegrator
    // ✅ Mantém a mesma lógica de verificação
    // =========================================================================

    /**
     * Verifica se uma CSS class possui estilo definido no DesignSystem.
     *
     * 🔧 v1.2: Implementação delegada ao Neumorphic System
     *
     * @param cssClass Classe CSS a verificar
     * @return true se a classe tem estilo associado, false caso contrário
     */
    public static boolean hasStyle(String cssClass) {
        if (cssClass == null || cssClass.isEmpty()) {
            return false;
        }

        // ✅ ÚNICA LINHA - DELEGAÇÃO COMPLETA
        return NeumorphicUISystem.hasStyle(cssClass);
    }

    // =========================================================================
    // 🧹 MÉTODOS DE UTILIDADE (v1.1+)
    // =========================================================================
    // ✅ v1.2: PERMANECEM IGUAIS - Não precisam de delegação
    // =========================================================================

    /**
     * Retorna todas as CSS classes suportadas pelo DesignSystem.
     * Útil para documentação e ferramentas de desenvolvimento.
     *
     * @since v1.1
     */
    public static String[] getSupportedClasses() {
        return new String[] {
                // Cards
                "metric-card", "neumorphic-card",
                // Botões
                "btn", "button", "neumorphic-button",
                // Containers
                "background-main", "neumorphic-panel", "neumorphic-inset",
                // Textos
                "text", "label", "heading-1", "heading-2", "heading-3",
                "heading-4", "heading-5", "body", "body-large", "body-small",
                "body-xs", "caption", "link",
                // Status
                "status-new", "status-updates", "status-review",
                "text-primary", "text-secondary", "text-muted",
                "text-email", "text-subtitle",
                // Tabelas
                "executions-table", "neumorphic-table", "table-header",
                "table-row", "table-cell",
                // Campos de texto
                "search-field", "neumorphic-textfield", "search-field-focused",
                // Árvore
                "tree-section", "tree-item", "tree-subitem", "tree-number",
                // Divisórias
                "divider", "separator", "divider-vertical"
        };
    }

    /**
     * Retorna o número total de classes CSS suportadas.
     *
     * @since v1.1
     */
    public static int getSupportedClassesCount() {
        return getSupportedClasses().length;
    }
}
