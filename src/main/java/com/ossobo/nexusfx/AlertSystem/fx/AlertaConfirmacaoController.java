package com.ossobo.nexusfx.AlertSystem.fx;

import com.ossobo.nexusfx.AlertSystem.core.ui.AlertaUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AlertaConfirmacaoController {

    @FXML private VBox containerPrincipal;
    @FXML private Label lblMensagem;
    @FXML private TextArea txtDetalhes;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private Stage stage;
    private Stage primaryStage; // ADICIONADO
    private Consumer<Boolean> callbackResposta;
    private String alertaId;

    @FXML
    public void initialize() {

        // Configurar ações dos botões
        if (btnConfirmar != null) {
            btnConfirmar.setOnAction(e -> onConfirmarAction());
        }

        if (btnCancelar != null) {
            btnCancelar.setOnAction(e -> onCancelarAction());
        }

        // Configurar teclas de atalho
        if (containerPrincipal != null) {
            containerPrincipal.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case ENTER:
                        onConfirmarAction();
                        break;
                    case ESCAPE:
                        onCancelarAction();
                        break;
                    case Y: // Y para Yes
                        if (!e.isControlDown()) onConfirmarAction();
                        break;
                    case N: // N para No
                        if (!e.isControlDown()) onCancelarAction();
                        break;
                }
            });
        }
    }

    public void configurarConfirmacao(String mensagem, String detalhes, String tipo) {

        if (lblMensagem != null) {
            lblMensagem.setText(mensagem);
        }

        if (detalhes != null && !detalhes.isEmpty()) {
            txtDetalhes.setText(detalhes);
            txtDetalhes.setVisible(true);
            txtDetalhes.setManaged(true);
        } else {
            txtDetalhes.setVisible(false);
            txtDetalhes.setManaged(false);
        }

        aplicarEstiloTipo(tipo);
    }

    private void aplicarEstiloTipo(String tipo) {
        if (containerPrincipal != null) {
            // Limpar estilos anteriores
            containerPrincipal.getStyleClass().removeIf(s ->
                    s.equals("perigo") || s.equals("aviso") ||
                            s.equals("info") || s.equals("sucesso"));

            if (tipo != null && !tipo.isEmpty()) {
                containerPrincipal.getStyleClass().add(tipo.toLowerCase());
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPrimaryStage(Stage primaryStage) { // ADICIONADO
        this.primaryStage = primaryStage;
    }

    public void setCallbackResposta(Consumer<Boolean> callback) {
        this.callbackResposta = callback;
    }

    public void setAlertaId(String id) {
        this.alertaId = id;
    }

    @FXML
    private void onConfirmarAction() {

        // 1. Remover overlay
        removerOverlay();

        // 2. Executar callback
        if (callbackResposta != null) {
            callbackResposta.accept(true);
        }

        // 3. Fechar janela
        fecharJanela();
    }

    @FXML
    private void onCancelarAction() {

        // 1. Remover overlay
        removerOverlay();

        // 2. Executar callback
        if (callbackResposta != null) {
            callbackResposta.accept(false);
        }

        // 3. Fechar janela
        fecharJanela();
    }

    /** Remove overlay de bloqueio */
    private void removerOverlay() {

        // Método 1: Usando AlertaUI
        if (stage != null) {
            AlertaUI.removerOverlayDoStage(stage);
        }

        // Método 2: Remover diretamente se o primeiro falhar
        if (primaryStage != null && primaryStage.getScene() != null &&
                primaryStage.getScene().getRoot() instanceof javafx.scene.layout.Pane) {

            javafx.scene.layout.Pane root = (javafx.scene.layout.Pane) primaryStage.getScene().getRoot();

            // Remover todos os overlays
            root.getChildren().removeIf(node ->
                    node instanceof javafx.scene.layout.Pane &&
                            node.getId() != null &&
                            node.getId().startsWith("alerta-overlay-")
            );

            root.requestLayout();
        }
    }

    private void fecharJanela() {

        if (stage != null) {
            stage.close();
        } else if (containerPrincipal != null && containerPrincipal.getScene() != null) {
            Stage currentStage = (Stage) containerPrincipal.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        }

        // Forçar foco de volta à janela principal
        if (primaryStage != null) {
            primaryStage.requestFocus();
        }
    }

    // Métodos para personalização
    public void setTextoBotaoConfirmar(String texto) {
        if (btnConfirmar != null) {
            btnConfirmar.setText(texto);
        }
    }

    public void setTextoBotaoCancelar(String texto) {
        if (btnCancelar != null) {
            btnCancelar.setText(texto);
        }
    }

    public void setCorBotaoConfirmar(String corHex) {
        if (btnConfirmar != null) {
            btnConfirmar.setStyle("-fx-background-color: " + corHex + ";");
        }
    }
}
