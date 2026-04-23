package com.ossobo.nexusfx.view.design.internal; /**
 * ===== CSSEngine.java (v2.0) =====
 *
 * PACOTE: com.ossobo.nexusfx.ViewEngine.design.internal
 *
 * RESPONSABILIDADE ÚNICA:
 * Carregar e aplicar arquivos .css via CSSLoader
 *
 * ANTES (v1.0): Resolução de paths + cache + aplicação
 * AGORA (v2.0): APENAS ponte entre StyleDefinition e CSSLoader
 */


import javafx.scene.Parent;

public final class CSSEngine {
    private static final CSSEngine INSTANCE = new CSSEngine();

    private CSSEngine() {}

    public static CSSEngine getInstance() {
        return INSTANCE;
    }

    /**
     * ===== applyCss() =====
     *
     * ✅ ÚNICO método público.
     * ✅ DELEGA para CSSLoader - ZERO lógica própria.
     * ✅ Chamado APENAS por StyleDefinition.
     */
    public void applyCss(Parent node, String cssPath) {
        if (node == null || cssPath == null) return;
        CSSLoader.loadCSS(node, cssPath);  // ← DELEGAÇÃO PURA
    }
}
