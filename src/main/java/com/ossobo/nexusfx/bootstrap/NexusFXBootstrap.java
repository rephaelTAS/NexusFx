package com.ossobo.nexusfx.bootstrap;

import com.ossobo.nexusfx.AlertSystem.core.AlertaSystem;
import com.ossobo.nexusfx.AlertSystem.sound.AlertaSons;
import com.ossobo.nexusfx.ImageManager.image.ImageRegistry;
import com.ossobo.nexusfx.Modaldialog.DialogOrchestrator;
import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.DiContainer;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.bootstrap.ResourceBootstrap;
import com.ossobo.nexusfx.view.ViewManager;
import com.ossobo.nexusfx.view.registry.ViewRegistry;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 NEXUS FX BOOTSTRAP - Inicializador central do framework
 *
 * v1.4 (23/04/2026):
 * - ✅ CORRIGIDO: Inicialização do DiContainer com proteção contra Reflections vazio
 * - ✅ CORRIGIDO: run() agora vincula bootstrap antes de iniciar JavaFX
 * - ✅ CORRIGIDO: initialize() com tratamento de erro robusto
 */
public final class NexusFXBootstrap {

    private static final Logger LOGGER = Logger.getLogger(NexusFXBootstrap.class.getName());

    private ResourceAPI resourceAPI;
    private DiContainer diContainer;
    private ImageRegistry imageRegistry;
    private ViewRegistry viewRegistry;
    private ViewManager viewManager;
    private AlertaSystem alertaSystem;
    private DialogOrchestrator dialogOrchestrator;

    private boolean initialized = false;
    private Stage primaryStage;
    private String scanPackages = "com.ossobo";
    private boolean enableDiagnostics = false;

    /**
     * ✅ Inicialização simplificada - única linha necessária
     */
    public static void run(Class<? extends Application> appClass) {
        String packageName = appClass.getPackageName();

        LOGGER.info("🎯 NexusFXBootstrap.run() - Iniciando com package: " + packageName);

        // Criar e SALVAR a instância
        NexusFXBootstrap instance = new NexusFXBootstrap()
                .withScanPackages(packageName)
                .withDiagnostics(true);

        // VINCULAR imediatamente ao NexusFX (antes do JavaFX iniciar)
        NexusFX.link(instance);
        LOGGER.info("✅ Bootstrap vinculado ao NexusFX");

        // Agora sim, iniciar o JavaFX
        LOGGER.info("🚀 Iniciando JavaFX...");
        Application.launch(appClass);
    }

    public NexusFXBootstrap withScanPackages(String packages) {
        this.scanPackages = (packages != null && !packages.trim().isEmpty()) ? packages : "com.ossobo";
        return this;
    }

    public NexusFXBootstrap withDiagnostics(boolean enable) {
        this.enableDiagnostics = enable;
        return this;
    }

    public void initialize(Stage primaryStage) {
        if (initialized) {
            LOGGER.warning("⚠️ NexusFX já foi inicializado. Ignorando...");
            return;
        }

        this.primaryStage = primaryStage;

        LOGGER.info("🚀 INICIALIZANDO NEXUS FX");

        try {
            // 1. Inicializar ResourceAPI
            resourceAPI = new ResourceAPI();
            ResourceBootstrap.bootstrap(resourceAPI);
            LOGGER.info("✅ ResourceAPI inicializado");

            // 2. Inicializar DiContainer COM PROTEÇÃO
            try {
                initializeDiContainer();
            } catch (Exception e) {
                LOGGER.warning("⚠️ Erro no DiContainer (não crítico para operação básica): " + e.getMessage());
                diContainer = null;
            }

            // 3. Inicializar ImageRegistry
            imageRegistry = new ImageRegistry();
            if (resourceAPI != null) {
                imageRegistry.setResourceAPI(resourceAPI);
            }
            LOGGER.info("✅ ImageRegistry inicializado");

            // 4. Inicializar ViewRegistry e ViewManager
            viewRegistry = ViewRegistry.getInstance();
            if (resourceAPI != null) {
                viewRegistry.setResourceAPI(resourceAPI);
            }
            viewManager = ViewManager.getInstance();
            LOGGER.info("✅ ViewRegistry e ViewManager inicializados");

            // 5. Inicializar AlertSystem
            initializeAlertSystem();

            // 6. Inicializar DialogOrchestrator
            try {
                dialogOrchestrator = DialogOrchestrator.getInstance();
                LOGGER.info("✅ DialogOrchestrator inicializado");
            } catch (Exception e) {
                LOGGER.warning("⚠️ Erro no DialogOrchestrator: " + e.getMessage());
                dialogOrchestrator = null;
            }

            // Atualizar vínculo com dados completos
            NexusFX.link(this);

            initialized = true;

            LOGGER.info("✅ NEXUS FX INICIALIZADO COM SUCESSO!");
            LOGGER.info("   📦 Recursos carregados: " + (resourceAPI != null ? resourceAPI.count() : 0));
            LOGGER.info("   📍 Pacotes escaneados: " + scanPackages);

            if (enableDiagnostics) {
                LOGGER.info("   🔍 Modo diagnóstico: ATIVADO");
                LOGGER.info("   🏗️ DiContainer: " + (diContainer != null ? "ATIVO" : "INATIVO (modo degradado)"));
                LOGGER.info("   🖼️ ImageRegistry: " + (imageRegistry != null ? "ATIVO" : "INATIVO"));
                LOGGER.info("   🪟 ViewManager: " + (viewManager != null ? "ATIVO" : "INATIVO"));
                LOGGER.info("   ⚠️ AlertSystem: " + (alertaSystem != null ? "ATIVO" : "INATIVO"));
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ FALHA NA INICIALIZAÇÃO DO NEXUS FX", e);
            initialized = true; // Marca como inicializado para evitar loops
            throw new RuntimeException("Falha ao inicializar NexusFX: " + e.getMessage(), e);
        }
    }

    /**
     * Inicialização segura do DiContainer
     */
    private void initializeDiContainer() {
        if (scanPackages == null || scanPackages.trim().isEmpty()) {
            LOGGER.warning("⚠️ scanPackages vazio, DiContainer não será inicializado");
            return;
        }

        try {
            LOGGER.info("🔍 Inicializando DiContainer com pacote: " + scanPackages);

            // Verifica se o pacote existe antes de tentar escanear
            if (isValidPackage(scanPackages)) {
                DiContainer.initialize(scanPackages);
                diContainer = DiContainer.getInstance();
                LOGGER.info("✅ DiContainer inicializado com sucesso");
            } else {
                LOGGER.warning("⚠️ Pacote '" + scanPackages + "' não encontrado no classpath");
            }

        } catch (Exception e) {
            LOGGER.warning("⚠️ Falha ao inicializar DiContainer: " + e.getMessage());

            // Tenta inicializar sem pacote específico
            try {
                LOGGER.info("🔄 Tentando inicializar DiContainer sem pacote específico...");
                DiContainer.initialize("com.ossobo"); // Pacote padrão do framework
                diContainer = DiContainer.getInstance();
                LOGGER.info("✅ DiContainer inicializado com pacote padrão");
            } catch (Exception e2) {
                LOGGER.warning("⚠️ Falha na segunda tentativa: " + e2.getMessage());
                throw e2;
            }
        }
    }

    /**
     * Inicialização segura do AlertSystem
     */
    private void initializeAlertSystem() {
        try {
            if (resourceAPI != null) {
                AlertaSons.setResourceAPI(resourceAPI);
                AlertaSons.inicializar();
            }

            alertaSystem = AlertaSystem.getInstance();

            if (resourceAPI != null) {
                alertaSystem.setResourceAPI(resourceAPI);
            }

            if (primaryStage != null) {
                alertaSystem.setPrimaryStage(primaryStage);
            }

            LOGGER.info("✅ AlertaSystem inicializado");

        } catch (Exception e) {
            LOGGER.warning("⚠️ Erro ao inicializar AlertaSystem: " + e.getMessage());
            alertaSystem = null;
        }
    }

    /**
     * Verifica se um pacote existe no classpath
     */
    private boolean isValidPackage(String packageName) {
        try {
            String path = packageName.replace('.', '/');
            java.net.URL resource = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(path);

            if (resource != null) {
                LOGGER.fine("✅ Pacote encontrado: " + packageName + " -> " + resource);
                return true;
            } else {
                LOGGER.fine("❌ Pacote não encontrado: " + packageName);
                return false;
            }
        } catch (Exception e) {
            LOGGER.fine("❌ Erro ao verificar pacote " + packageName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void shutdown() {
        if (!initialized) return;

        LOGGER.info("🔻 Desligando NexusFX...");

        if (alertaSystem != null) {
            try {
                alertaSystem.fecharTodosAlertas();
            } catch (Exception e) {
                LOGGER.warning("⚠️ Erro ao fechar alertas: " + e.getMessage());
            }
        }

        if (viewManager != null) {
            try {
                viewManager.clearCache();
            } catch (Exception e) {
                LOGGER.warning("⚠️ Erro ao limpar cache de views: " + e.getMessage());
            }
        }

        if (diContainer != null) {
            try {
                diContainer.close();
            } catch (Exception e) {
                LOGGER.warning("⚠️ Erro ao fechar DiContainer: " + e.getMessage());
            }
        }

        initialized = false;
        LOGGER.info("✅ NexusFX desligado com sucesso");
    }

    // Getters
    public ResourceAPI getResourceAPI() { return resourceAPI; }
    public DiContainer getDiContainer() { return diContainer; }
    public ImageRegistry getImageRegistry() { return imageRegistry; }
    public ViewRegistry getViewRegistry() { return viewRegistry; }
    public ViewManager getViewManager() { return viewManager; }
    public AlertaSystem getAlertaSystem() { return alertaSystem; }
    public Stage getPrimaryStage() { return primaryStage; }
}