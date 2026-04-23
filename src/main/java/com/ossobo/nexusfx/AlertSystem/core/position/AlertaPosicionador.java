package com.ossobo.nexusfx.AlertSystem.core.position;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Responsável pelo posicionamento inteligente dos alertas
 * Coesão forte - apenas lógica de posicionamento
 */
public class AlertaPosicionador {

    /** Posiciona o alerta de forma inteligente */
    public static void posicionar(Stage alertStage, Node ownerNode, Stage primaryStage,
                                  double largura, double altura) {
        if (ownerNode != null) {
            posicionarRelativoAoNode(alertStage, ownerNode, largura, altura);
        } else if (primaryStage != null) {
            posicionarNoCentroPrincipal(alertStage, primaryStage, largura, altura);
        } else {
            posicionarNoCantoTela(alertStage, largura, altura);
        }
    }

    /** Posiciona relativo a um nó específico */
    private static void posicionarRelativoAoNode(Stage alertStage, Node node,
                                                 double largura, double altura) {
        Window ownerWindow = node.getScene().getWindow();
        Point2D nodePos = node.localToScreen(0, 0);

        double x = nodePos.getX() + node.getBoundsInLocal().getWidth() / 2 - largura / 2;
        double y = nodePos.getY() + node.getBoundsInLocal().getHeight() / 2 - altura / 2;

        alertStage.setX(Math.max(0, x));
        alertStage.setY(Math.max(0, y));
    }

    /** Posiciona no centro da janela principal */
    private static void posicionarNoCentroPrincipal(Stage alertStage, Stage primary,
                                                    double largura, double altura) {
        double centerX = primary.getX() + primary.getWidth() / 2 - largura / 2;
        double centerY = primary.getY() + primary.getHeight() / 2 - altura / 2;

        alertStage.setX(centerX);
        alertStage.setY(centerY);
    }

    /** Posiciona no canto da tela quando não há referência */
    private static void posicionarNoCantoTela(Stage alertStage, double largura, double altura) {
        Screen tela = Screen.getPrimary();
        Rectangle2D bounds = tela.getVisualBounds();

        alertStage.setX(bounds.getMaxX() - largura - 20);
        alertStage.setY(20);
    }


    /** Obtém o nó raiz para overlay */
    public static Node obterRootNode(Node ownerNode, Stage primaryStage) {
        if (ownerNode != null && ownerNode.getScene() != null) {
            return ownerNode.getScene().getRoot();
        } else if (primaryStage != null && primaryStage.getScene() != null) {
            return primaryStage.getScene().getRoot();
        }
        return null;
    }

    /** Configura bindings do overlay */
    public static void configurarBindings(VBox overlay, Pane root) {
        if (root instanceof Region) {
            overlay.prefWidthProperty().bind(((Region) root).widthProperty());
            overlay.prefHeightProperty().bind(((Region) root).heightProperty());
        }
    }
}
