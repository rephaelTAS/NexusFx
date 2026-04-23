package com.ossobo.nexusfx.resources.bootstrap;

import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.descriptor.AlertDescriptor;
import com.ossobo.nexusfx.resources.descriptor.ImageDescription;
import com.ossobo.nexusfx.resources.descriptor.ResourceDescriptor;
import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 RESOURCE BOOTSTRAP - Inicializador de recursos de alertas
 *
 * v1.0 (22/04/2026):
 * - ✅ Registra FXMLs, CSS, ícones e sons dos alertas
 * - ✅ Usa os assets existentes em /com/ossobo/nexusfx/assets/
 * - ✅ Executado após ResourceAPI.initialize()
 */
public final class ResourceBootstrap {
    private static final Logger LOGGER = Logger.getLogger(ResourceBootstrap.class.getName());

    // ===== PATHS BASE =====
    private static final String BASE_ASSETS = "/com/ossobo/nexusfx/assets/";
    private static final String BASE_FXML = "/com/ossobo/nexusfx/fxml/";
    private static final String BASE_STYLE = "/com/ossobo/nexusfx/style/";

    // ===== ÍCONES =====
    private static final String ICON_CONFIRM = BASE_ASSETS + "icons/confirm.png";
    private static final String ICON_CRITICAL = BASE_ASSETS + "icons/critical.png";
    private static final String ICON_ERROR = BASE_ASSETS + "icons/error.png";
    private static final String ICON_INFO = BASE_ASSETS + "icons/info.png";
    private static final String ICON_SUCCESS = BASE_ASSETS + "icons/success.png";
    private static final String ICON_WARNING = BASE_ASSETS + "icons/warning.png";

    // ===== SONS =====
    private static final String SOUND_CONFIRMATION = BASE_ASSETS + "sound/confirmation.mp3";
    private static final String SOUND_CRITICAL = BASE_ASSETS + "sound/critical.mp3";
    private static final String SOUND_ERROR = BASE_ASSETS + "sound/error.mp3";
    private static final String SOUND_INFO = BASE_ASSETS + "sound/info.mp3";
    private static final String SOUND_WARNING = BASE_ASSETS + "sound/warning.mp3";

    // ===== FXMLs =====
    private static final String FXML_CONFIRMACAO = BASE_FXML + "alerta-confirmacao.fxml";
    private static final String FXML_DETALHES = BASE_FXML + "alerta-detalhes.fxml";
    private static final String FXML_MODAL = BASE_FXML + "alerta-modal.fxml";
    private static final String FXML_NAOMODAL = BASE_FXML + "alerta-naomodal.fxml";
    private static final String FXML_SEMIMODAL = BASE_FXML + "alerta-semimodal.fxml";

    // ===== CSS =====
    private static final String CSS_CONFIRMACAO = BASE_STYLE + "alerta-confirmacao.css";
    private static final String CSS_INFO = BASE_STYLE + "alerta-info.css";
    private static final String CSS_MODAL = BASE_STYLE + "alerta-modal.css";
    private static final String CSS_ALERTAS = BASE_STYLE + "alertas.css";
    private static final String CSS_DETAILS = BASE_STYLE + "detailss.css";
    private static final String CSS_NEUMORPHIC = BASE_STYLE + "neumorphic.css";

    private static boolean bootstrapped = false;

    private ResourceBootstrap() {
        // Classe utilitária - não instanciar
    }

    /**
     * ✅ Inicializa os recursos de alertas do framework.
     *
     * @param resourceAPI API de recursos já inicializada
     */
    public static void bootstrap(ResourceAPI resourceAPI) {
        if (bootstrapped) {
            LOGGER.warning("ResourceBootstrap já foi executado. Ignorando...");
            return;
        }

        if (resourceAPI == null) {
            throw new IllegalArgumentException("ResourceAPI não pode ser nulo");
        }

        LOGGER.info("🚀 Iniciando bootstrap de recursos de alertas do NexusFX...");
        long startTime = System.currentTimeMillis();

        int registered = 0;

        // 1. Registrar Ícones
        registered += registrarIcones(resourceAPI);

        // 2. Registrar Sons
        registered += registrarSons(resourceAPI);

        // 3. Registrar CSS
        registered += registrarCss(resourceAPI);

        // 4. Registrar FXMLs como AlertDescriptors
        registered += registrarAlertas(resourceAPI);

        bootstrapped = true;
        long elapsed = System.currentTimeMillis() - startTime;

        LOGGER.info(String.format("✅ Bootstrap concluído em %d ms. %d recursos de alertas registrados.",
                elapsed, registered));
    }

    // ===== REGISTRO DE ÍCONES =====

    private static int registrarIcones(ResourceAPI api) {
        int count = 0;

        count += registrarIcone(api, "fx-icon-confirm", ICON_CONFIRM, "Ícone de confirmação");
        count += registrarIcone(api, "fx-icon-critical", ICON_CRITICAL, "Ícone crítico");
        count += registrarIcone(api, "fx-icon-error", ICON_ERROR, "Ícone de erro");
        count += registrarIcone(api, "fx-icon-info", ICON_INFO, "Ícone de informação");
        count += registrarIcone(api, "fx-icon-success", ICON_SUCCESS, "Ícone de sucesso");
        count += registrarIcone(api, "fx-icon-warning", ICON_WARNING, "Ícone de aviso");

        return count;
    }

    private static int registrarIcone(ResourceAPI api, String id, String path, String description) {
        try {
            URL url = ResourceBootstrap.class.getResource(path);
            if (url == null) {
                LOGGER.warning("❌ Ícone não encontrado: " + path);
                return 0;
            }

            ImageDescription descriptor = new ImageDescription(
                    id,
                    url,
                    ImageDescription.ImageType.ICON,
                    32, 32,
                    true,
                    true,
                    description,
                    ResourceOrigin.FRAMEWORK
            );

            api.register(descriptor);
            LOGGER.fine(() -> "✅ Ícone registrado: " + id);
            return 1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Falha ao registrar ícone '" + id + "': " + e.getMessage());
            return 0;
        }
    }

    // ===== REGISTRO DE SONS =====

    private static int registrarSons(ResourceAPI api) {
        int count = 0;

        count += registrarSom(api, "fx-sound-confirmation", SOUND_CONFIRMATION);
        count += registrarSom(api, "fx-sound-critical", SOUND_CRITICAL);
        count += registrarSom(api, "fx-sound-error", SOUND_ERROR);
        count += registrarSom(api, "fx-sound-info", SOUND_INFO);
        count += registrarSom(api, "fx-sound-warning", SOUND_WARNING);

        return count;
    }

    private static int registrarSom(ResourceAPI api, String id, String path) {
        try {
            URL url = ResourceBootstrap.class.getResource(path);
            if (url == null) {
                LOGGER.warning("❌ Som não encontrado: " + path);
                return 0;
            }

            ResourceDescriptor descriptor = new ResourceDescriptor(id, url, ResourceType.SOUND, ResourceOrigin.FRAMEWORK) {};

            api.register(descriptor);
            LOGGER.fine(() -> "✅ Som registrado: " + id);
            return 1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Falha ao registrar som '" + id + "': " + e.getMessage());
            return 0;
        }
    }

    // ===== REGISTRO DE CSS =====

    private static int registrarCss(ResourceAPI api) {
        int count = 0;

        count += registrarCss(api, "fx-css-confirmacao", CSS_CONFIRMACAO);
        count += registrarCss(api, "fx-css-info", CSS_INFO);
        count += registrarCss(api, "fx-css-modal", CSS_MODAL);
        count += registrarCss(api, "fx-css-alertas", CSS_ALERTAS);
        count += registrarCss(api, "fx-css-details", CSS_DETAILS);
        count += registrarCss(api, "fx-css-neumorphic", CSS_NEUMORPHIC);

        return count;
    }

    private static int registrarCss(ResourceAPI api, String id, String path) {
        try {
            URL url = ResourceBootstrap.class.getResource(path);
            if (url == null) {
                LOGGER.warning("❌ CSS não encontrado: " + path);
                return 0;
            }

            ResourceDescriptor descriptor = new ResourceDescriptor(id, url, ResourceType.CSS, ResourceOrigin.FRAMEWORK) {};

            api.register(descriptor);
            LOGGER.fine(() -> "✅ CSS registrado: " + id);
            return 1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Falha ao registrar CSS '" + id + "': " + e.getMessage());
            return 0;
        }
    }

    // ===== REGISTRO DE ALERTAS (AlertDescriptor) =====

    private static int registrarAlertas(ResourceAPI api) {
        int count = 0;

        // Alerta de Confirmação
        count += registrarAlerta(api,
                "fx-alert-confirm",
                FXML_CONFIRMACAO,
                AlertDescriptor.AlertType.CONFIRMATION,
                AlertDescriptor.Modality.APPLICATION_MODAL,
                "fx-sound-confirmation",
                "fx-icon-confirm",
                true,
                0
        );

        // Alerta de Erro (Crítico)
        count += registrarAlerta(api,
                "fx-alert-critical",
                FXML_MODAL,
                AlertDescriptor.AlertType.ERROR,
                AlertDescriptor.Modality.APPLICATION_MODAL,
                "fx-sound-critical",
                "fx-icon-critical",
                false,
                0
        );

        // Alerta de Erro
        count += registrarAlerta(api,
                "fx-alert-error",
                FXML_MODAL,
                AlertDescriptor.AlertType.ERROR,
                AlertDescriptor.Modality.APPLICATION_MODAL,
                "fx-sound-error",
                "fx-icon-error",
                false,
                0
        );

        // Alerta de Informação
        count += registrarAlerta(api,
                "fx-alert-info",
                FXML_NAOMODAL,
                AlertDescriptor.AlertType.INFO,
                AlertDescriptor.Modality.NONE,
                "fx-sound-info",
                "fx-icon-info",
                false,
                3000
        );

        // Alerta de Sucesso
        count += registrarAlerta(api,
                "fx-alert-success",
                FXML_NAOMODAL,
                AlertDescriptor.AlertType.SUCCESS,
                AlertDescriptor.Modality.NONE,
                null,
                "fx-icon-success",
                false,
                2500
        );

        // Alerta de Aviso
        count += registrarAlerta(api,
                "fx-alert-warning",
                FXML_SEMIMODAL,
                AlertDescriptor.AlertType.WARNING,
                AlertDescriptor.Modality.WINDOW_MODAL,
                "fx-sound-warning",
                "fx-icon-warning",
                false,
                4000
        );

        // Alerta com Detalhes
        count += registrarAlerta(api,
                "fx-alert-details",
                FXML_DETALHES,
                AlertDescriptor.AlertType.ERROR,
                AlertDescriptor.Modality.APPLICATION_MODAL,
                "fx-sound-error",
                "fx-icon-error",
                false,
                0
        );

        return count;
    }

    private static int registrarAlerta(ResourceAPI api,
                                       String id,
                                       String fxmlPath,
                                       AlertDescriptor.AlertType alertType,
                                       AlertDescriptor.Modality modality,
                                       String soundId,
                                       String iconId,
                                       boolean confirmationRequired,
                                       long autoCloseMillis) {
        try {
            URL fxmlUrl = ResourceBootstrap.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                LOGGER.warning("❌ FXML não encontrado: " + fxmlPath);
                return 0;
            }

            URL soundUrl = null;
            if (soundId != null) {
                soundUrl = api.find(soundId).map(ResourceDescriptor::getUrl).orElse(null);
            }

            URL iconUrl = null;
            if (iconId != null) {
                iconUrl = api.find(iconId).map(ResourceDescriptor::getUrl).orElse(null);
            }

            AlertDescriptor descriptor = new AlertDescriptor(
                    id,
                    fxmlUrl,
                    alertType,
                    modality,
                    soundUrl,
                    iconUrl,
                    confirmationRequired,
                    autoCloseMillis,
                    ResourceOrigin.FRAMEWORK
            );

            api.register(descriptor);
            LOGGER.fine(() -> "✅ Alerta registrado: " + id);
            return 1;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Falha ao registrar alerta '" + id + "': " + e.getMessage());
            return 0;
        }
    }

    // ===== DIAGNÓSTICO =====

    public static boolean isBootstrapped() {
        return bootstrapped;
    }

    /**
     * ✅ Diagnóstico dos recursos de alertas registrados
     */
    public static void diagnose(ResourceAPI api) {
        System.out.println("\n🔍 RESOURCE BOOTSTRAP - ALERTAS");
        System.out.println("=".repeat(50));
        System.out.println("📊 RECURSOS REGISTRADOS:");
        System.out.println("• Ícones: " + api.listIdsByType(ResourceType.IMAGE).stream()
                .filter(id -> id.startsWith("fx-icon-")).count());
        System.out.println("• Sons: " + api.listIdsByType(ResourceType.SOUND).stream()
                .filter(id -> id.startsWith("fx-sound-")).count());
        System.out.println("• CSS: " + api.listIdsByType(ResourceType.CSS).stream()
                .filter(id -> id.startsWith("fx-css-")).count());
        System.out.println("• Alertas: " + api.listIdsByType(ResourceType.ALERT).stream()
                .filter(id -> id.startsWith("fx-alert-")).count());
        System.out.println("\n📋 ALERTAS DISPONÍVEIS:");
        api.listIdsByType(ResourceType.ALERT).stream()
                .filter(id -> id.startsWith("fx-alert-"))
                .forEach(id -> {
                    api.getAlertDescriptor(id).ifPresent(alert ->
                            System.out.println("  • " + id + " [" + alert.getAlertType() + "]")
                    );
                });
        System.out.println("=".repeat(50));
    }
}
