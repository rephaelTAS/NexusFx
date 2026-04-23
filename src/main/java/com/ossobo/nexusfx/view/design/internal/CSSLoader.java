package com.ossobo.nexusfx.view.design.internal; /**
 * ===== CSSLoader.java =====
 *
 * PACOTE: com.ossobo.nexusfx.ViewEngine.design.internal
 *
 * RESPONSABILIDADE ÚNICA:
 * Encontrar e resolver caminhos de arquivos .css
 *
 * NÃO FAZ:
 * - Não aplica estilo
 * - Não gerencia cache
 * - Não tem estado
 * - Não decide estratégia
 *
 * ✅ APENAS uma coisa: encontrar CSS
 */


import javafx.scene.Parent;

import java.net.URL;

public final class CSSLoader {
    private static final String[] CLASS_PATH_BASES = {
            "/css/", "/styles/", "/view/css/", "/fxml/css/", "/packt/css/"
    };

    private CSSLoader() {} // Utility class - sem instância

    /**
     * ===== loadCSS() =====
     *
     * ✅ ÚNICO método público.
     * ✅ Encontra e adiciona CSS ao node.
     * ✅ Chamado APENAS por CSSEngine.
     */
    public static void loadCSS(Parent node, String cssPath) {
        if (node == null || cssPath == null) return;

        String fullPath = resolveCSS(cssPath);
        if (fullPath != null && !node.getStylesheets().contains(fullPath)) {
            node.getStylesheets().add(fullPath);
        }
    }

    /**
     * ===== resolveCSS() =====
     *
     * Package-private para testes.
     * Busca em múltiplos locais padrão.
     */
    static String resolveCSS(String relativePath) {
        String normalizedPath = relativePath.startsWith("/")
                ? relativePath.substring(1)
                : relativePath;

        // 1. Tenta caminho exato
        URL cssUrl = CSSLoader.class.getResource("/" + normalizedPath);
        if (cssUrl != null) return cssUrl.toExternalForm();

        cssUrl = CSSLoader.class.getResource(relativePath);
        if (cssUrl != null) return cssUrl.toExternalForm();

        // 2. Tenta com bases pré-definidas
        for (String base : CLASS_PATH_BASES) {
            String path = base + normalizedPath;
            cssUrl = CSSLoader.class.getResource(path);
            if (cssUrl != null) return cssUrl.toExternalForm();
        }

        return null;
    }

    /**
     * ===== cssExists() =====
     */
    public static boolean cssExists(String cssPath) {
        return resolveCSS(cssPath) != null;
    }
}
