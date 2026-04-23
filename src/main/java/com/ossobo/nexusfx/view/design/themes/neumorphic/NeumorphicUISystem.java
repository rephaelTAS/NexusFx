/**
 * ===== NeumorphicDesignSystemIntegrator.java =====
 * PACOTE: com.ossobo.nexusfx.ViewEngine.design.themes.neumorphic
 *
 * RESPONSABILIDADE ÚNICA:
 * IMPLEMENTAÇÃO COMPLETA DE TODOS OS ESTILOS NEUMORPHIC
 *
 * =========================================================================
 * 📌 COMO USAR:
 * =========================================================================
 *
 * 1️⃣ COLE ESTE ARQUIVO EM: 
 *    com/ossobo/nexusfx/ViewEngine/design/themes/neumorphic/
 *
 * 2️⃣ APAGUE TODOS OS OUTROS APPLIERS:
 *    - NeumorphicCardApplier.java
 *    - NeumorphicButtonApplier.java
 *    - NeumorphicTextApplier.java
 *    - NeumorphicTableViewApplier.java
 *    - NeumorphicTextFieldApplier.java
 *    - NeumorphicTreeApplier.java
 *    - NeumorphicDividerApplier.java
 *    - NeumorphicPanelApplier.java
 *
 * 3️⃣ PRONTO! SEU DesignSystem.java AGORA USA ESTE INTEGRADOR
 *
 * =========================================================================
 * @version 1.2 - Implementação única e completa
 * @since 12/02/2026
 * =========================================================================
 */

package com.ossobo.nexusfx.view.design.themes.neumorphic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 🎨 IMPLEMENTADOR ÚNICO - TODOS OS ESTILOS NEUMORPHIC EM UM ARQUIVO SÓ!
 *
 * SEU DesignSystem.java CHAMA ESTA CLASSE - NADA MAIS É NECESSÁRIO!
 */
public final class NeumorphicUISystem {

    // =========================================================================
    // 🎨 CORES E TOKENS
    // =========================================================================
    private static final Color COR_BASE = Color.rgb(224, 229, 236);
    private static final Color COR_FUNDO = Color.rgb(238, 240, 244);
    private static final Color COR_SUPERFICIE = Color.rgb(224, 229, 236);
    private static final Color COR_TEXTO_PRIMARIO = Color.rgb(74, 78, 92);
    private static final Color COR_TEXTO_SECUNDARIO = Color.rgb(122, 126, 140);
    private static final Color COR_TEXTO_DESABILITADO = Color.rgb(176, 180, 192);
    private static final Color COR_TEXTO_EMAIL = Color.rgb(100, 120, 140);
    private static final Color COR_TEXTO_SUBTITULO = Color.rgb(140, 144, 158);
    private static final Color COR_TEXTO_MUTED = Color.rgb(160, 164, 176);
    private static final Color COR_SOMBRA_CLARA = Color.rgb(255, 255, 255, 0.9);
    private static final Color COR_SOMBRA_ESCURA = Color.rgb(163, 177, 198, 0.5);
    private static final Color COR_SOMBRA_ESCURA_FORTE = Color.rgb(163, 177, 198, 0.7);

    private static final Color COR_STATUS_NEW = Color.rgb(76, 175, 80);
    private static final Color COR_STATUS_UPDATES = Color.rgb(33, 150, 243);
    private static final Color COR_STATUS_REVIEW = Color.rgb(255, 152, 0);

    // =========================================================================
    // 📏 ESPAÇAMENTOS
    // =========================================================================
    private static final int ESPACO_XS = 4;
    private static final int ESPACO_SM = 8;
    private static final int ESPACO_MD = 16;
    private static final int ESPACO_LG = 24;
    private static final int ESPACO_XL = 32;

    // =========================================================================
    // 📐 RAIO DAS BORDAS
    // =========================================================================
    private static final int RAIO_PEQUENO = 4;
    private static final int RAIO_MEDIO = 8;
    private static final int RAIO_GRANDE = 12;
    private static final int RAIO_EXTRA_GRANDE = 20;
    private static final int RAIO_CIRCULO = 999;

    // =========================================================================
    // 🎭 NÍVEIS DE SOMBRA
    // =========================================================================
    private static final int SOMBRA_NIVEL_1 = 3;
    private static final int SOMBRA_NIVEL_2 = 5;
    private static final int SOMBRA_NIVEL_3 = 8;
    private static final int SOMBRA_NIVEL_4 = 12;
    private static final int SOMBRA_NIVEL_5 = 16;

    // =========================================================================
    // 🚀 CONSTRUTOR PRIVADO
    // =========================================================================
    private NeumorphicUISystem() {}

    // =========================================================================
    // 🎯 MÉTODO PRINCIPAL - CHAMADO PELO SEU DesignSystem.java
    // =========================================================================
    public static void aplicar(Node node, String cssClass) {
        if (node == null || cssClass == null || cssClass.isEmpty()) {
            return;
        }

        switch (cssClass) {
            // ===== CARDS =====
            case "metric-card" -> applyMetricCard(node);
            case "neumorphic-card" -> applyNeumorphicCard(node);

            // ===== BOTÕES =====
            case "btn", "button", "neumorphic-button" ->
                    applyNeumorphicButton(node);

            // ===== CONTAINERS =====
            case "background-main" ->
                    applyBackgroundMain(node);
            case "neumorphic-panel" ->
                    applyNeumorphicPanel(node);
            case "neumorphic-inset" ->
                    applyNeumorphicInset(node);

            // ===== TEXTOS =====
            case "text", "label", "heading-1", "heading-2", "heading-3",
                 "heading-4", "heading-5", "body", "body-large", "body-small",
                 "body-xs", "caption", "link" ->
                    applyNeumorphicText(node, cssClass);

            // ===== STATUS COLORS =====
            case "status-new", "status-updates", "status-review",
                 "text-primary", "text-secondary", "text-muted",
                 "text-email", "text-subtitle" ->
                    applyNeumorphicTextColor(node, cssClass);

            // ===== TABELAS =====
            case "executions-table", "neumorphic-table" ->
                    applyNeumorphicTable(node);
            case "table-header" ->
                    applyNeumorphicTableHeader(node);
            case "table-row" ->
                    applyNeumorphicTableRow(node);
            case "table-cell" ->
                    applyNeumorphicTableCell(node);

            // ===== CAMPOS DE PESQUISA =====
            case "search-field", "neumorphic-textfield" ->
                    applyNeumorphicTextField(node);
            case "search-field-focused" ->
                    applyNeumorphicTextFieldFocused(node);

            // ===== ÁRVORE DE DIRETÓRIOS =====
            case "tree-section" ->
                    applyNeumorphicTreeSection(node);
            case "tree-item" ->
                    applyNeumorphicTreeItem(node);
            case "tree-subitem" ->
                    applyNeumorphicTreeSubItem(node);
            case "tree-number" ->
                    applyNeumorphicTreeNumber(node);

            // ===== DIVISÓRIAS =====
            case "divider", "separator" ->
                    applyNeumorphicDivider(node);
            case "divider-vertical" ->
                    applyNeumorphicDividerVertical(node);

            // ===== SEM ESTILO =====
            default -> {}
        }
    }

    // =========================================================================
    // 🔍 VERIFICADOR - CHAMADO PELO SEU DesignSystem.java
    // =========================================================================
    public static boolean hasStyle(String cssClass) {
        if (cssClass == null || cssClass.isEmpty()) {
            return false;
        }

        return switch (cssClass) {
            case "metric-card", "neumorphic-card",
                 "btn", "button", "neumorphic-button",
                 "background-main", "neumorphic-panel", "neumorphic-inset",
                 "text", "label", "heading-1", "heading-2", "heading-3",
                 "heading-4", "heading-5", "body", "body-large", "body-small",
                 "body-xs", "caption", "link",
                 "status-new", "status-updates", "status-review",
                 "text-primary", "text-secondary", "text-muted",
                 "text-email", "text-subtitle",
                 "executions-table", "neumorphic-table",
                 "table-header", "table-row", "table-cell",
                 "search-field", "neumorphic-textfield", "search-field-focused",
                 "tree-section", "tree-item", "tree-subitem", "tree-number",
                 "divider", "separator", "divider-vertical" -> true;
            default -> false;
        };
    }

    // =========================================================================
    // 🃏 CARDS
    // =========================================================================
    private static void applyMetricCard(Node node) {
        if (!(node instanceof Region r)) return;
        r.setBackground(createBackground(COR_SUPERFICIE, RAIO_GRANDE));
        r.setEffect(createOuterShadow(SOMBRA_NIVEL_3));
        r.setPadding(new Insets(ESPACO_MD));
        if (r instanceof VBox v) {
            v.setSpacing(ESPACO_XS);
            v.setAlignment(Pos.CENTER);
        }
    }

    private static void applyNeumorphicCard(Node node) {
        if (!(node instanceof Region r)) return;
        r.setBackground(createBackground(COR_SUPERFICIE, RAIO_GRANDE));
        r.setEffect(createOuterShadow(SOMBRA_NIVEL_3));
        r.setPadding(new Insets(ESPACO_LG));
    }

    // =========================================================================
    // 🔘 BOTÕES
    // =========================================================================
    private static void applyNeumorphicButton(Node node) {
        if (!(node instanceof ButtonBase b)) return;

        b.setBackground(createBackground(COR_SUPERFICIE, RAIO_MEDIO));
        b.setTextFill(COR_TEXTO_PRIMARIO);
        b.setFont(Font.font("System", FontWeight.NORMAL, 13));
        b.setPadding(new Insets(ESPACO_SM, ESPACO_LG, ESPACO_SM, ESPACO_LG));
        b.setEffect(createOuterShadow(SOMBRA_NIVEL_2));

        b.setOnMouseEntered(e -> {
            b.setEffect(createOuterShadow(SOMBRA_NIVEL_3));
            b.setTranslateY(-1);
        });
        b.setOnMouseExited(e -> {
            b.setEffect(createOuterShadow(SOMBRA_NIVEL_2));
            b.setTranslateY(0);
        });
        b.setOnMousePressed(e -> {
            b.setEffect(createInnerShadow(SOMBRA_NIVEL_2));
            b.setTranslateY(1);
        });
        b.setOnMouseReleased(e -> {
            b.setEffect(createOuterShadow(SOMBRA_NIVEL_2));
            b.setTranslateY(0);
        });

        b.disabledProperty().addListener((obs, o, n) -> {
            if (n) {
                b.setOpacity(0.6);
                b.setEffect(null);
                b.setTextFill(COR_TEXTO_DESABILITADO);
            } else {
                b.setOpacity(1.0);
                b.setEffect(createOuterShadow(SOMBRA_NIVEL_2));
                b.setTextFill(COR_TEXTO_PRIMARIO);
            }
        });
    }

    // =========================================================================
    // 🧩 PANELS
    // =========================================================================
    private static void applyBackgroundMain(Node node) {
        if (node instanceof Region r) {
            r.setBackground(createBackground(COR_FUNDO, 0));
        }
    }

    private static void applyNeumorphicPanel(Node node) {
        if (node instanceof Region r) {
            r.setBackground(createBackground(COR_SUPERFICIE, RAIO_GRANDE));
            r.setEffect(createOuterShadow(SOMBRA_NIVEL_2));
            r.setPadding(new Insets(ESPACO_LG));
        }
    }

    private static void applyNeumorphicInset(Node node) {
        if (node instanceof Region r) {
            r.setBackground(createBackground(COR_SUPERFICIE, RAIO_GRANDE));
            r.setEffect(createInnerShadow(SOMBRA_NIVEL_2));
            r.setPadding(new Insets(ESPACO_LG));
        }
    }

    // =========================================================================
    // 📝 TEXTOS
    // =========================================================================
    private static void applyNeumorphicText(Node node, String cssClass) {
        if (!(node instanceof Labeled l)) return;

        switch (cssClass) {
            case "heading-1" -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", FontWeight.BOLD, 24)); }
            case "heading-2" -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", FontWeight.BOLD, 20)); }
            case "heading-3" -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", FontWeight.BOLD, 18)); }
            case "heading-4" -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", FontWeight.BOLD, 16)); }
            case "heading-5" -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", FontWeight.BOLD, 14)); }
            case "body" -> { l.setTextFill(COR_TEXTO_SECUNDARIO); l.setFont(Font.font("System", 13)); }
            case "body-large" -> { l.setTextFill(COR_TEXTO_SECUNDARIO); l.setFont(Font.font("System", 15)); }
            case "body-small" -> { l.setTextFill(COR_TEXTO_SECUNDARIO); l.setFont(Font.font("System", 12)); }
            case "body-xs" -> { l.setTextFill(COR_TEXTO_SECUNDARIO); l.setFont(Font.font("System", 11)); }
            case "caption" -> { l.setTextFill(COR_TEXTO_MUTED); l.setFont(Font.font("System", 11)); }
            case "link" -> {
                l.setTextFill(Color.rgb(33, 150, 243));
                l.setFont(Font.font("System", FontWeight.MEDIUM, 13));
                l.setUnderline(true);
                l.setOnMouseEntered(e -> l.setTextFill(Color.rgb(25, 118, 210)));
                l.setOnMouseExited(e -> l.setTextFill(Color.rgb(33, 150, 243)));
            }
            default -> { l.setTextFill(COR_TEXTO_PRIMARIO); l.setFont(Font.font("System", 13)); }
        }
    }

    // =========================================================================
    // 🎨 CORES DE TEXTO
    // =========================================================================
    private static void applyNeumorphicTextColor(Node node, String cssClass) {
        if (!(node instanceof Labeled l)) return;

        switch (cssClass) {
            case "status-new" -> { l.setTextFill(COR_STATUS_NEW); l.setFont(Font.font("System", FontWeight.BOLD, 12)); }
            case "status-updates" -> { l.setTextFill(COR_STATUS_UPDATES); l.setFont(Font.font("System", FontWeight.BOLD, 12)); }
            case "status-review" -> { l.setTextFill(COR_STATUS_REVIEW); l.setFont(Font.font("System", FontWeight.BOLD, 12)); }
            case "text-primary" -> l.setTextFill(COR_TEXTO_PRIMARIO);
            case "text-secondary" -> l.setTextFill(COR_TEXTO_SECUNDARIO);
            case "text-muted" -> l.setTextFill(COR_TEXTO_MUTED);
            case "text-email" -> l.setTextFill(COR_TEXTO_EMAIL);
            case "text-subtitle" -> l.setTextFill(COR_TEXTO_SUBTITULO);
        }
    }

    // =========================================================================
    // 📊 TABELAS
    // =========================================================================
    private static void applyNeumorphicTable(Node node) {
        if (!(node instanceof TableView t)) return;
        t.setBackground(createBackground(COR_SUPERFICIE, RAIO_MEDIO));
        t.setEffect(createInnerShadow(SOMBRA_NIVEL_1));
        t.setPadding(new Insets(ESPACO_SM));
    }

    private static void applyNeumorphicTableHeader(Node node) {
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_PRIMARIO);
            l.setFont(Font.font("System", FontWeight.BOLD, 12));
            l.setPadding(new Insets(ESPACO_SM));
        }
    }

    private static void applyNeumorphicTableRow(Node node) {
        if (node instanceof Region r) {
            r.setBackground(createBackground(Color.TRANSPARENT, 0));
            r.setPadding(new Insets(ESPACO_XS, ESPACO_SM, ESPACO_XS, ESPACO_SM));
        }
    }

    private static void applyNeumorphicTableCell(Node node) {
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_SECUNDARIO);
            l.setFont(Font.font("System", 12));
        }
    }

    // =========================================================================
    // 🔍 TEXT FIELDS
    // =========================================================================
    private static void applyNeumorphicTextField(Node node) {
        if (!(node instanceof TextInputControl t)) return;
        t.setBackground(createBackground(COR_SUPERFICIE, RAIO_MEDIO));
        t.setEffect(createInnerShadow(SOMBRA_NIVEL_1));
        t.setPadding(new Insets(ESPACO_SM, ESPACO_MD, ESPACO_SM, ESPACO_MD));
        t.setFont(Font.font("System", 13));

        t.focusedProperty().addListener((obs, o, n) -> {
            if (n) {
                t.setEffect(createInnerShadow(SOMBRA_NIVEL_2));
                t.setBackground(createBackground(COR_SUPERFICIE.brighter(), RAIO_MEDIO));
            } else {
                t.setEffect(createInnerShadow(SOMBRA_NIVEL_1));
                t.setBackground(createBackground(COR_SUPERFICIE, RAIO_MEDIO));
            }
        });
    }

    private static void applyNeumorphicTextFieldFocused(Node node) {
        if (!(node instanceof TextInputControl t)) return;
        applyNeumorphicTextField(node);
        t.setEffect(createInnerShadow(SOMBRA_NIVEL_2));
        t.setBackground(createBackground(COR_SUPERFICIE.brighter(), RAIO_MEDIO));
    }

    // =========================================================================
    // 🌳 TREE
    // =========================================================================
    private static void applyNeumorphicTreeSection(Node node) {
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_PRIMARIO);
            l.setFont(Font.font("System", FontWeight.BOLD, 13));
            l.setPadding(new Insets(ESPACO_SM, 0, ESPACO_SM, 0));
        }
    }

    private static void applyNeumorphicTreeItem(Node node) {
        if (node instanceof HBox h) {
            h.setSpacing(ESPACO_SM);
            h.setPadding(new Insets(ESPACO_XS, 0, ESPACO_XS, ESPACO_MD));
            h.setAlignment(Pos.CENTER_LEFT);
        }
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_SECUNDARIO);
            l.setFont(Font.font("System", 12));
        }
    }

    private static void applyNeumorphicTreeSubItem(Node node) {
        if (node instanceof HBox h) {
            h.setSpacing(ESPACO_SM);
            h.setPadding(new Insets(2, 0, 2, ESPACO_XL));
            h.setAlignment(Pos.CENTER_LEFT);
        }
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_MUTED);
            l.setFont(Font.font("System", 12));
        }
    }

    private static void applyNeumorphicTreeNumber(Node node) {
        if (node instanceof Labeled l) {
            l.setTextFill(COR_TEXTO_PRIMARIO);
            l.setFont(Font.font("System", FontWeight.BOLD, 12));
            l.setPadding(new Insets(2, ESPACO_SM, 2, ESPACO_SM));
            if (node instanceof Region r) {
                r.setBackground(createBackground(COR_SUPERFICIE.darker(), RAIO_PEQUENO));
            }
        }
    }

    // =========================================================================
    // ➖ DIVIDERS
    // =========================================================================
    private static void applyNeumorphicDivider(Node node) {
        if (node instanceof Region r) {
            r.setMinHeight(1);
            r.setPrefHeight(1);
            r.setMaxHeight(1);
            r.setBackground(createBackground(COR_SOMBRA_ESCURA.deriveColor(0, 1, 1, 0.2), 0));
            r.setEffect(createInnerShadow(1));
        }
    }

    private static void applyNeumorphicDividerVertical(Node node) {
        if (node instanceof Region r) {
            r.setMinWidth(1);
            r.setPrefWidth(1);
            r.setMaxWidth(1);
            r.setBackground(createBackground(COR_SOMBRA_ESCURA.deriveColor(0, 1, 1, 0.2), 0));
        }
    }

    // =========================================================================
    // 🛠️ UTILITÁRIOS DE BACKGROUND
    // =========================================================================
    private static Background createBackground(Color cor, double raio) {
        return new Background(new BackgroundFill(
                cor, new CornerRadii(raio), Insets.EMPTY
        ));
    }

    // =========================================================================
    // 🛠️ UTILITÁRIOS DE SOMBRA
    // =========================================================================
    private static Effect createOuterShadow(int tamanho) {
        DropShadow escura = new DropShadow();
        escura.setRadius(tamanho);
        escura.setOffsetX(tamanho / 2);
        escura.setOffsetY(tamanho / 2);
        escura.setColor(COR_SOMBRA_ESCURA);

        DropShadow clara = new DropShadow();
        clara.setRadius(tamanho);
        clara.setOffsetX(-tamanho / 2);
        clara.setOffsetY(-tamanho / 2);
        clara.setColor(COR_SOMBRA_CLARA);

        Blend blend = new Blend(BlendMode.SRC_OVER);
        blend.setBottomInput(escura);
        blend.setTopInput(clara);
        return blend;
    }

    private static Effect createInnerShadow(int tamanho) {
        InnerShadow escura = new InnerShadow();
        escura.setRadius(tamanho);
        escura.setOffsetX(tamanho / 2);
        escura.setOffsetY(tamanho / 2);
        escura.setColor(COR_SOMBRA_ESCURA_FORTE);

        InnerShadow clara = new InnerShadow();
        clara.setRadius(tamanho);
        clara.setOffsetX(-tamanho / 2);
        clara.setOffsetY(-tamanho / 2);
        clara.setColor(COR_SOMBRA_CLARA);

        Blend blend = new Blend(BlendMode.SRC_OVER);
        blend.setBottomInput(escura);
        blend.setTopInput(clara);
        return blend;
    }
}
