package com.ossobo.nexusfx.AlertSystem.core;

import com.ossobo.nexusfx.AlertSystem.core.animation.AlertaAnimador;
import com.ossobo.nexusfx.AlertSystem.core.position.AlertaPosicionador;
import com.ossobo.nexusfx.AlertSystem.core.ui.AlertaUI;
import com.ossobo.nexusfx.AlertSystem.fx.AlertaConfirmacaoController;
import com.ossobo.nexusfx.AlertSystem.fx.AlertaController;
import com.ossobo.nexusfx.AlertSystem.model.*;
import com.ossobo.nexusfx.AlertSystem.sound.AlertaSons;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.descriptor.AlertDescriptor;
import com.ossobo.nexusfx.resources.descriptor.ResourceDescriptor;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * Sistema central de gerenciamento de alertas inteligentes
 * Integrado com ResourceAPI (datacenter)
 *
 * v2.1 (23/04/2026):
 * - ✅ Suporte a ModelAlert
 * - ✅ Métodos de conveniência: info(), warn(), erro(), critico()
 * - ✅ Busca FXMLs, ícones e sons do ResourceAPI
 * - ✅ Mantém compatibilidade com API existente
 */
public class AlertaSystem {
    private static AlertaSystem instance;
    private Stage primaryStage;
    private final Map<String, AlertInfo> alertasAtivos = new HashMap<>();
    private final Map<String, Timeline> timelines = new HashMap<>();

    private ResourceAPI resourceAPI;

    private static final Duration TEMPO_SEMIMODAL = Duration.seconds(5);
    private static final Duration TEMPO_NAO_MODAL = Duration.seconds(3);

    private static final String ALERT_MODAL = "fx-alert-critical";
    private static final String ALERT_SEMIMODAL = "fx-alert-warning";
    private static final String ALERT_NAOMODAL = "fx-alert-info";
    private static final String ALERT_CONFIRM = "fx-alert-confirm";
    private static final String ALERT_DETAILS = "fx-alert-details";
    private static final String ALERT_SUCCESS = "fx-alert-success";

    private AlertaSystem() {
        inicializarSons();
    }

    public static synchronized AlertaSystem getInstance() {
        if (instance == null) {
            instance = new AlertaSystem();
        }
        return instance;
    }

    // ==================== INTEGRAÇÃO COM RESOURCE API ====================

    public void setResourceAPI(ResourceAPI api) {
        this.resourceAPI = api;
        AlertaSons.setResourceAPI(api);
        System.out.println("✅ ResourceAPI vinculado ao AlertaSystem");
    }

    private URL obterUrlFXML(Modalidade modalidade) {
        if (resourceAPI == null) return null;

        String resourceId = switch (modalidade) {
            case MODAL -> ALERT_MODAL;
            case SEMI_MODAL -> ALERT_SEMIMODAL;
            case NAO_MODAL -> ALERT_NAOMODAL;
            default -> ALERT_SEMIMODAL;
        };

        try {
            if (resourceAPI.exists(resourceId, ResourceType.ALERT)) {
                return resourceAPI.getAlertUrl(resourceId);
            }
        } catch (Exception e) {
            // Fallback para paths locais
        }
        return null;
    }

    private URL obterUrlFXMLConfirmacao() {
        if (resourceAPI != null && resourceAPI.exists(ALERT_CONFIRM, ResourceType.ALERT)) {
            try { return resourceAPI.getAlertUrl(ALERT_CONFIRM); } catch (Exception e) {}
        }
        return null;
    }

    private URL obterUrlFXMLDetalhes() {
        if (resourceAPI != null && resourceAPI.exists(ALERT_DETAILS, ResourceType.ALERT)) {
            try { return resourceAPI.getAlertUrl(ALERT_DETAILS); } catch (Exception e) {}
        }
        return null;
    }

    private Optional<AlertDescriptor> obterAlertDescriptor(String resourceId) {
        if (resourceAPI == null) return Optional.empty();
        return resourceAPI.getAlertDescriptor(resourceId);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // ==================== MÉTODOS PÚBLICOS PRINCIPAIS ====================

    /**
     * ✅ NOVO: Cria alerta a partir de ModelAlert
     */
    public String criarAlerta(ModelAlert modelAlert) {
        return criarAlerta(
                modelAlert.getTitulo(),
                modelAlert.getDescricao(),
                modelAlert.getDetalhes(),
                modelAlert.getOrigem(),
                modelAlert.getOwnerNode(),
                modelAlert.getTipo(),
                modelAlert.getModalidade()
        );
    }

    /** Cria alerta com todos parâmetros configuráveis */
    public String criarAlerta(String titulo, String descricao, String detalhes,
                              String origem, Node ownerNode,
                              TipoAlerta tipo, Modalidade modalidade) {
        String id = UUID.randomUUID().toString();
        Platform.runLater(() -> {
            processarCriacaoAlertaComId(id, titulo, descricao, detalhes,
                    origem, ownerNode, tipo, modalidade);
        });
        return id;
    }

    // ===== MÉTODOS DE CONVENIÊNCIA =====

    /** Alerta de informação */
    public String info(String titulo, String descricao) {
        return criarAlerta(ModelAlert.info(titulo, descricao));
    }

    /** Alerta de informação com detalhes */
    public String info(String titulo, String descricao, String detalhes) {
        return criarAlerta(ModelAlert.builder()
                .titulo(titulo).descricao(descricao).detalhes(detalhes).info().build());
    }

    /** Alerta de aviso */
    public String warn(String titulo, String descricao) {
        return criarAlerta(ModelAlert.warn(titulo, descricao));
    }

    /** Alerta de aviso com detalhes */
    public String warn(String titulo, String descricao, String detalhes) {
        return criarAlerta(ModelAlert.builder()
                .titulo(titulo).descricao(descricao).detalhes(detalhes).warn().build());
    }

    /** Alerta de erro */
    public String erro(String titulo, String descricao) {
        return criarAlerta(ModelAlert.erro(titulo, descricao));
    }

    /** Alerta de erro com detalhes */
    public String erro(String titulo, String descricao, String detalhes) {
        return criarAlerta(ModelAlert.builder()
                .titulo(titulo).descricao(descricao).detalhes(detalhes).erro().build());
    }

    /** Alerta crítico */
    public String critico(String titulo, String descricao) {
        return criarAlerta(ModelAlert.critico(titulo, descricao));
    }

    /** Alerta crítico com detalhes */
    public String critico(String titulo, String descricao, String detalhes) {
        return criarAlerta(ModelAlert.builder()
                .titulo(titulo).descricao(descricao).detalhes(detalhes).critico().build());
    }

    // ===== GERENCIAMENTO =====

    public void fecharAlerta(String id) {
        Platform.runLater(() -> processarFechamentoAlerta(id));
    }

    public void fecharTodosAlertas() {
        Platform.runLater(() -> {
            new ArrayList<>(alertasAtivos.keySet()).forEach(this::processarFechamentoAlerta);
        });
    }

    public int getQuantidadeAlertasAtivos() {
        return alertasAtivos.size();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    // ===== MÉTODOS DE CONFIRMAÇÃO =====


    public String criarConfirmacao(String mensagem, String detalhes,
                                   String origem, Node ownerNode,
                                   TipoConfirmacao tipo, Consumer<Boolean> callbackResposta) {
        String id = UUID.randomUUID().toString();
        Platform.runLater(() -> {
            try {
                criarConfirmacaoComFXML(id, mensagem, detalhes, origem, ownerNode, tipo, callbackResposta);
            } catch (Exception e) {
                criarConfirmacaoFallback(id, mensagem, detalhes, origem, ownerNode, tipo, callbackResposta);
            }
        });
        return id;
    }

    public void confirmar(String mensagem, String titulo, Consumer<Boolean> callback) {
        criarConfirmacao(mensagem, null, titulo, null, TipoConfirmacao.PADRAO, callback);
    }

    public void confirmarComDetalhes(String mensagem, String detalhes, String titulo,
                                     TipoConfirmacao tipo, Consumer<Boolean> callback) {
        criarConfirmacao(mensagem, detalhes, titulo, null, tipo, callback);
    }

    // ===== PROCESSAMENTO INTERNO =====

    private void processarCriacaoAlertaComId(String id, String titulo, String descricao, String detalhes,
                                             String origem, Node ownerNode,
                                             TipoAlerta tipo, Modalidade modalidade) {
        try {
            criarAlertaComFXML(id, titulo, descricao, detalhes, origem, ownerNode, tipo, modalidade);
        } catch (Exception e) {
            criarAlertaFallback(id, titulo, descricao, detalhes, origem, ownerNode, tipo, modalidade);
        }
    }

    private void criarAlertaComFXML(String id, String titulo, String descricao, String detalhes,
                                    String origem, Node ownerNode,
                                    TipoAlerta tipo, Modalidade modalidade) throws IOException {

        URL fxmlUrl = obterUrlFXML(modalidade);

        FXMLLoader loader;
        if (fxmlUrl != null) {
            loader = new FXMLLoader(fxmlUrl);
        } else {
            String caminhoFXML = obterCaminhoFXMLFallback(modalidade);
            loader = new FXMLLoader(getClass().getResource(caminhoFXML));
        }

        Parent root = loader.load();
        AlertaController controller = loader.getController();

        Stage alertStage = configurarStage(modalidade);
        alertStage.initStyle(StageStyle.UNDECORATED);
        alertStage.setTitle("Alerta - " + titulo);

        if (controller != null) {
            controller.configurarAlerta(titulo, descricao, origem, detalhes, tipo.name());
            controller.setAlertaId(id);
            controller.setAlertaStage(alertStage);
            controller.setPrimaryStage(primaryStage);
            controller.setOnCloseCallback(() -> fecharAlerta(id));

            if (modalidade != Modalidade.MODAL) {
                Duration tempo = modalidade == Modalidade.NAO_MODAL ? TEMPO_NAO_MODAL : TEMPO_SEMIMODAL;
                controller.configurarTemporizador((int) tempo.toSeconds());
            }
        }

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        alertStage.setScene(scene);

        Pane overlay = null;
        if (modalidade != Modalidade.NAO_MODAL) {
            overlay = AlertaUI.criarOverlayBloqueio(ownerNode, primaryStage);
        }

        double largura = root.prefWidth(-1) > 0 ? root.prefWidth(-1) : 350;
        double altura = root.prefHeight(-1) > 0 ? root.prefHeight(-1) : 150;
        AlertaPosicionador.posicionar(alertStage, ownerNode, primaryStage, largura, altura);

        AlertaSons.tocarSom(tipo);

        alertStage.show();
        registrarAlerta(id, alertStage, tipo, modalidade, ownerNode, overlay);

        if (modalidade != Modalidade.MODAL && controller != null) {
            configurarAutoFechamentoFXML(id, alertStage, modalidade, controller);
        }
    }

    private String obterCaminhoFXMLFallback(Modalidade modalidade) {
        return switch (modalidade) {
            case MODAL -> "/com/ossobo/nexusfx/fxml/alerta-modal.fxml";
            case SEMI_MODAL -> "/com/ossobo/nexusfx/fxml/alerta-semimodal.fxml";
            case NAO_MODAL -> "/com/ossobo/nexusfx/fxml/alerta-naomodal.fxml";
            default -> "/com/ossobo/nexusfx/fxml/alerta-semimodal.fxml";
        };
    }

    private void criarConfirmacaoComFXML(String id, String mensagem, String detalhes,
                                         String origem, Node ownerNode,
                                         TipoConfirmacao tipo, Consumer<Boolean> callbackResposta) throws IOException {

        URL fxmlUrl = obterUrlFXMLConfirmacao();

        FXMLLoader loader;
        if (fxmlUrl != null) {
            loader = new FXMLLoader(fxmlUrl);
        } else {
            String caminhoFXML = "/com/ossobo/nexusfx/fxml/alerta-confirmacao.fxml";
            loader = new FXMLLoader(getClass().getResource(caminhoFXML));
        }

        Parent root = loader.load();
        AlertaConfirmacaoController controller = loader.getController();

        Stage confirmStage = new Stage();
        confirmStage.initStyle(StageStyle.UNDECORATED);
        confirmStage.setTitle("Confirmação - " + origem);
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        if (primaryStage != null) confirmStage.initOwner(primaryStage);

        final Pane overlay = AlertaUI.criarOverlayBloqueio(ownerNode, primaryStage);

        if (controller != null) {
            controller.configurarConfirmacao(mensagem, detalhes, tipo.name());
            controller.setAlertaId(id);
            controller.setStage(confirmStage);
            controller.setPrimaryStage(primaryStage);

            controller.setCallbackResposta(resposta -> {
                confirmStage.close();
                if (overlay != null) AlertaUI.removerOverlayBloqueio(overlay);
                alertasAtivos.remove(id);
                if (callbackResposta != null) callbackResposta.accept(resposta);
                if (primaryStage != null) primaryStage.requestFocus();
            });
        }

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        confirmStage.setScene(scene);

        double largura = root.prefWidth(-1) > 0 ? root.prefWidth(-1) : 400;
        double altura = root.prefHeight(-1) > 0 ? root.prefHeight(-1) : 220;

        AlertaPosicionador.posicionar(confirmStage, ownerNode, primaryStage, largura, altura);
        AlertaSons.tocarSomConfirmacao(tipo);

        AlertInfo info = new AlertInfo(id, confirmStage, TipoAlerta.INFO, Modalidade.MODAL, ownerNode, overlay);
        alertasAtivos.put(id, info);

        confirmStage.setOnHidden(e -> {
            if (overlay != null) AlertaUI.removerOverlayBloqueio(overlay);
            alertasAtivos.remove(id);
            if (primaryStage != null) primaryStage.requestFocus();
        });

        confirmStage.show();
    }

    // ===== MÉTODOS MANTIDOS =====

    private void criarConfirmacaoFallback(String id, String mensagem, String detalhes,
                                          String origem, Node ownerNode,
                                          TipoConfirmacao tipo, Consumer<Boolean> callbackResposta) {
        VBox conteudo = AlertaUI.criarConteudoConfirmacao(mensagem, detalhes, tipo, callbackResposta);
        Stage confirmStage = configurarStageConfirmacao(ownerNode);
        confirmStage.setTitle("Confirmação - " + origem);
        Scene cena = new Scene(conteudo);
        cena.setFill(javafx.scene.paint.Color.TRANSPARENT);
        confirmStage.setScene(cena);
        Pane overlay = AlertaUI.criarOverlayBloqueio(ownerNode, primaryStage);
        AlertaPosicionador.posicionar(confirmStage, ownerNode, primaryStage,
                conteudo.getPrefWidth(), conteudo.getPrefHeight());
        registrarConfirmacao(id, confirmStage, overlay, ownerNode);
        confirmStage.show();
        AlertaSons.tocarSomConfirmacao(tipo);
    }

    private Stage configurarStageConfirmacao(Node ownerNode) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (primaryStage != null) stage.initOwner(primaryStage);
        return stage;
    }

    private void registrarConfirmacao(String id, Stage stage, Pane overlay, Node ownerNode) {
        AlertInfo info = new AlertInfo(id, stage, TipoAlerta.INFO, Modalidade.MODAL, ownerNode, overlay);
        alertasAtivos.put(id, info);
        stage.setOnHidden(e -> processarFechamentoAlerta(id));
    }

    private void criarAlertaFallback(String id, String titulo, String descricao, String detalhes,
                                     String origem, Node ownerNode,
                                     TipoAlerta tipo, Modalidade modalidade) {
        VBox conteudo = AlertaUI.criarConteudo(titulo, descricao, detalhes, origem, tipo);
        Stage alertStage = configurarStage(modalidade);
        Pane overlay = configurarOverlay(modalidade, ownerNode);
        configurarExibicaoFallback(id, alertStage, conteudo, overlay, tipo, modalidade, ownerNode);
    }

    private void configurarExibicaoFallback(String id, Stage stage, VBox conteudo,
                                            Pane overlay, TipoAlerta tipo,
                                            Modalidade modalidade, Node ownerNode) {
        Scene cena = new Scene(conteudo);
        cena.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(cena);
        AlertaPosicionador.posicionar(stage, ownerNode, primaryStage,
                conteudo.getPrefWidth(), conteudo.getPrefHeight());
        AlertaAnimador.animarEntrada(conteudo);
        AlertaSons.tocarSom(tipo);
        stage.show();
        configurarAutoFechamento(id, stage, modalidade);
        registrarAlerta(id, stage, tipo, modalidade, ownerNode, overlay);
    }

    private void configurarAutoFechamentoFXML(String id, Stage stage,
                                              Modalidade modalidade, AlertaController controller) {
        Duration tempo = modalidade == Modalidade.NAO_MODAL ? TEMPO_NAO_MODAL : TEMPO_SEMIMODAL;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (controller != null) controller.atualizarTemporizador();
                }),
                new KeyFrame(tempo, e -> fecharAlerta(id))
        );
        timeline.setCycleCount((int) tempo.toSeconds());
        timeline.play();
        timelines.put(id, timeline);
    }

    private Stage configurarStage(Modalidade modalidade) {
        Stage stage = new Stage();
        switch (modalidade) {
            case MODAL -> stage.initModality(Modality.APPLICATION_MODAL);
            case SEMI_MODAL -> {
                stage.initModality(Modality.WINDOW_MODAL);
                if (primaryStage != null) stage.initOwner(primaryStage);
            }
            case NAO_MODAL -> stage.initModality(Modality.NONE);
        }
        return stage;
    }

    private Pane configurarOverlay(Modalidade modalidade, Node ownerNode) {
        if (modalidade != Modalidade.NAO_MODAL) {
            return AlertaUI.criarOverlayBloqueio(ownerNode, primaryStage);
        }
        return null;
    }

    private void configurarAutoFechamento(String id, Stage stage, Modalidade modalidade) {
        if (modalidade != Modalidade.MODAL) {
            Duration tempo = modalidade == Modalidade.NAO_MODAL ? TEMPO_NAO_MODAL : TEMPO_SEMIMODAL;
            Timeline timeline = new Timeline(new KeyFrame(tempo, e -> fecharAlerta(id)));
            timeline.play();
            timelines.put(id, timeline);
        }
    }

    private void registrarAlerta(String id, Stage stage, TipoAlerta tipo,
                                 Modalidade modalidade, Node ownerNode, Pane overlay) {
        AlertInfo info = new AlertInfo(id, stage, tipo, modalidade, ownerNode, overlay);
        alertasAtivos.put(id, info);
    }

    private void processarFechamentoAlerta(String id) {
        AlertInfo info = alertasAtivos.get(id);
        if (info != null) {
            Timeline timeline = timelines.remove(id);
            if (timeline != null) timeline.stop();
            removerOverlayBloqueioSeguro(info);
            if (info.stage != null && info.stage.isShowing()) {
                try {
                    if (info.stage.getScene() != null) {
                        Parent root = info.stage.getScene().getRoot();
                        if (root != null) {
                            FadeTransition fade = new FadeTransition(Duration.millis(200), root);
                            fade.setFromValue(1.0);
                            fade.setToValue(0.0);
                            fade.setOnFinished(e -> {
                                info.stage.close();
                                limparRegistros(id);
                            });
                            fade.play();
                        } else {
                            info.stage.close();
                            limparRegistros(id);
                        }
                    } else {
                        info.stage.close();
                        limparRegistros(id);
                    }
                } catch (Exception e) {
                    info.stage.close();
                }
            }
        }
    }

    private void removerOverlayBloqueioSeguro(AlertInfo info) {
        if (info.overlayBlock != null) {
            try {
                AlertaUI.removerOverlayBloqueio(info.overlayBlock);
                if (info.stage != null) AlertaUI.removerOverlayDoStage(info.stage);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Parent parent = info.overlayBlock.getParent();
                    if (parent instanceof Pane container) {
                        if (container.getChildren().contains(info.overlayBlock)) {
                            container.getChildren().remove(info.overlayBlock);
                            container.requestLayout();
                        }
                    }
                });
            }
        }
    }

    private void limparRegistros(String id) {
        alertasAtivos.remove(id);
        timelines.remove(id);
    }

    private void inicializarSons() {
        AlertaSons.inicializar();
    }

    // ===== ENUMS E CLASSES INTERNAS =====



    // ===== MÉTODOS UTILITÁRIOS =====

    public boolean isAlertaAtivo(String id) {
        return alertasAtivos.containsKey(id);
    }

    public List<String> listarAlertasAtivos() {
        List<String> lista = new ArrayList<>();
        for (AlertInfo info : alertasAtivos.values()) {
            lista.add(String.format("[%s] %s - %s",
                    info.tipo, info.modalidade, info.id.substring(0, 8)));
        }
        return lista;
    }

    public void setVolume(double volume) {
        AlertaSons.setVolumeGeral(volume);
    }

    public void limparTodosOverlays() {
        for (AlertInfo info : alertasAtivos.values()) {
            if (info.overlayBlock != null) {
                AlertaUI.removerOverlayBloqueio(info.overlayBlock);
            }
        }
        if (primaryStage != null && primaryStage.getScene() != null &&
                primaryStage.getScene().getRoot() instanceof Pane root) {
            List<Node> nodesParaRemover = new ArrayList<>();
            for (Node node : root.getChildren()) {
                if (node instanceof Pane && node.getId() != null &&
                        node.getId().startsWith("alerta-overlay-")) {
                    nodesParaRemover.add(node);
                }
            }
            root.getChildren().removeAll(nodesParaRemover);
            root.requestLayout();
        }
    }

    // ==================== DIAGNÓSTICO ====================

    public void diagnosticarSistema() {
        System.out.println("\n🔍 ALERTA SYSTEM - DIAGNÓSTICO");
        System.out.println("=".repeat(50));
        System.out.println("• ResourceAPI: " + (resourceAPI != null ? "✅ Vinculado" : "❌ Não vinculado"));
        System.out.println("• Alertas ativos: " + alertasAtivos.size());
        System.out.println("• PrimaryStage: " + (primaryStage != null ? "✅" : "❌"));

        if (resourceAPI != null) {
            System.out.println("\n📋 ALERTAS NO RESOURCE API:");
            resourceAPI.listIdsByType(ResourceType.ALERT).forEach(id ->
                    System.out.println("  • " + id + " → " + resourceAPI.exists(id))
            );
        }
        System.out.println("=".repeat(50));
    }
}
