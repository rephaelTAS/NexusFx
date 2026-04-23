package com.ossobo.nexusfx;

import com.ossobo.nexusfx.AlertSystem.core.AlertaSystem;
import com.ossobo.nexusfx.ImageManager.image.ImageRegistry;
import com.ossobo.nexusfx.bootstrap.NexusFXBootstrap;
import com.ossobo.nexusfx.di.DiContainer;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.view.ViewManager;
import com.ossobo.nexusfx.view.registry.ViewRegistry;

import javafx.stage.Stage;

public final class NexusFX {

    static NexusFXBootstrap bootstrap;  // Alterado para package-private

    private NexusFX() {}

    /**
     * ✅ Vincula o bootstrap (chamado internamente)
     */
    public static void link(NexusFXBootstrap bootstrap) {
        NexusFX.bootstrap = bootstrap;

        // Log para debug
        if (bootstrap != null) {
            System.out.println("✅ NexusFX.link() - Bootstrap vinculado: " + bootstrap.getClass().getSimpleName());
        }
    }

    /**
     * Retorna o bootstrap para inicialização tardia
     */
    public static NexusFXBootstrap getBootstrap() {
        return bootstrap;
    }

    // Métodos de acesso com verificação
    public static ResourceAPI resources() {
        if (bootstrap == null) {
            throw new IllegalStateException("❌ NexusFX não foi inicializado! Chame NexusFXBootstrap.run() primeiro.");
        }
        return bootstrap.getResourceAPI();
    }

    public static ViewManager views() {
        return bootstrap.getViewManager();
    }

    public static ViewRegistry viewRegistry() {
        return bootstrap.getViewRegistry();
    }

    public static AlertaSystem alerts() {
        return bootstrap.getAlertaSystem();
    }

    public static ImageRegistry images() {
        return bootstrap.getImageRegistry();
    }

    public static DiContainer di() {
        return bootstrap.getDiContainer();
    }

    public static Stage stage() {
        AlertaSystem alertas = alerts();
        return alertas != null ? alertas.getPrimaryStage() : null;
    }

    public static void shutdown() {
        if (bootstrap != null) bootstrap.shutdown();
    }
}