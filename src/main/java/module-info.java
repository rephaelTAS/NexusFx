/**
 * Módulo NexusFX - Framework JavaFX
 * v7.2.2
 *
 * Baseado na estrutura real de diretórios.
 */
module com.ossobo.nexusfx {

    // ===== REQUIRES =====
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires transitive javafx.media;

    requires org.slf4j;
    requires java.sql;
    requires org.reflections;

    // =============================================
    // EXPORTS - CORE (com.ossobo.nexusfx)
    // =============================================
    exports com.ossobo.nexusfx;                         // NexusFX.java
    exports com.ossobo.nexusfx.bootstrap;                // NexusFXBootstrap.java
    exports com.ossobo.nexusfx.exceptions;               // BusinessRuleException, etc.
    exports com.ossobo.nexusfx.core;                     // NexusErrorHandler.java

    // =============================================
    // EXPORTS - ALERT SYSTEM
    // =============================================
    exports com.ossobo.nexusfx.AlertSystem;              // SystemsAlerty.java
    exports com.ossobo.nexusfx.AlertSystem.core;         // AlertaSystem.java
    exports com.ossobo.nexusfx.AlertSystem.core.animation; // AlertaAnimador.java
    exports com.ossobo.nexusfx.AlertSystem.core.position;  // AlertaPosicionador.java
    exports com.ossobo.nexusfx.AlertSystem.core.ui;      // AlertaUI.java
    exports com.ossobo.nexusfx.AlertSystem.fx;           // AlertaController, AlertaConfirmacaoController
    exports com.ossobo.nexusfx.AlertSystem.model;        // ModelAlert, AlertInfo, Modalidade, TipoAlerta
    exports com.ossobo.nexusfx.AlertSystem.sound;        // AlertaSons.java

    // =============================================
    // EXPORTS - DI CONTAINER
    // =============================================
    exports com.ossobo.nexusfx.di;                       // DiContainer.java
    exports com.ossobo.nexusfx.di.annotations;           // @Component, @Service, @Inject, etc.
    exports com.ossobo.nexusfx.di.exceptions;            // CircularDependencyException, etc.
    exports com.ossobo.nexusfx.di.scopes;                // ScopeType, SingletonScope, etc.

    // =============================================
    // EXPORTS - IMAGE MANAGER
    // =============================================
    exports com.ossobo.nexusfx.ImageManager;             // ImageService.java
    exports com.ossobo.nexusfx.ImageManager.image;       // ImageRegistry, ImageCache, etc.

    // =============================================
    // EXPORTS - MODAL DIALOGS
    // =============================================
    exports com.ossobo.nexusfx.Modaldialog;              // DialogOrchestrator, etc.

    // =============================================
    // EXPORTS - NOTIFICATIONS
    // =============================================
    exports com.ossobo.nexusfx.notifications.animation;   // AnimationManager, AnimationType
    exports com.ossobo.nexusfx.notifications.builder;     // NotificationBuilder
    exports com.ossobo.nexusfx.notifications.descriptor;  // NotificationDescriptor
    exports com.ossobo.nexusfx.notifications.exceptions;  // NotificationException
    exports com.ossobo.nexusfx.notifications.manager;     // NotificationManager
    exports com.ossobo.nexusfx.notifications.model;       // NotificationResult
    exports com.ossobo.nexusfx.notifications.position;    // PositionManager, ScreenPosition
    exports com.ossobo.nexusfx.notifications.presenter;   // NotificationPresenter
    exports com.ossobo.nexusfx.notifications.renderer;    // NotificationRenderer
    exports com.ossobo.nexusfx.notifications.types;       // NotificationType

    // =============================================
    // EXPORTS - OWNER WINDOW PROVIDER
    // =============================================
    exports com.ossobo.nexusfx.OwnerWindowProvider;      // OwnerWindowProvider.java

    // =============================================
    // EXPORTS - RESOURCES (DATACENTER)
    // =============================================
    exports com.ossobo.nexusfx.resources.api;             // ResourceAPI.java
    exports com.ossobo.nexusfx.resources.bootstrap;       // ResourceBootstrap.java
    exports com.ossobo.nexusfx.resources.cache;           // ResourceCache.java
    exports com.ossobo.nexusfx.resources.descriptor;      // ViewDescriptor, ImageDescription, AlertDescriptor, ResourceDescriptor
    exports com.ossobo.nexusfx.resources.enums;           // ResourceType, ResourceOrigin
    exports com.ossobo.nexusfx.resources.excecoes;        // ResourceException, etc.
    exports com.ossobo.nexusfx.resources.guard;           // ResourceGuard.java
    exports com.ossobo.nexusfx.resources.loader;          // ResourceLoader.java
    exports com.ossobo.nexusfx.resources.registry;        // ResourceRegistry.java
    exports com.ossobo.nexusfx.resources.resolver;        // ResourceResolver.java

    // =============================================
    // EXPORTS - VIEW SYSTEM
    // =============================================
    exports com.ossobo.nexusfx.view;                     // ViewManager.java
    exports com.ossobo.nexusfx.view.design;              // DesignSystem, StyleDefinition
    exports com.ossobo.nexusfx.view.design.internal;     // CSSEngine, CSSLoader
    exports com.ossobo.nexusfx.view.design.themes.neumorphic; // NeumorphicUISystem
    exports com.ossobo.nexusfx.view.exceptios;           // ViewEngineException
    exports com.ossobo.nexusfx.view.loader;              // FXMLService
    exports com.ossobo.nexusfx.view.refresh;             // RefreshManager, RefreshableController
    exports com.ossobo.nexusfx.view.registry;            // ViewRegistry
    exports com.ossobo.nexusfx.view.views;               // LoadedView
    exports com.ossobo.nexusfx.view.views.config;        // FXMLConfig

    // =============================================
    // ABERTURA PARA REFLEXÃO (FXML)
    // =============================================
    opens com.ossobo.nexusfx.view to javafx.fxml;
    opens com.ossobo.nexusfx.AlertSystem to javafx.fxml;
}