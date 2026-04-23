package com.ossobo.nexusfx.view;

/**
 * ===== ViewManager.java (v8.4) =====
 * PACOTE: com.ossobo.nexusfx.view
 *
 * v8.4 - ADAPTADO AO ViewDescriptor CORRETO (resources.descriptor)
 * ✅ Usa com.ossobo.nexusfx.resources.descriptor.ViewDescriptor
 * ✅ Métodos adaptados: getId(), getFxmlUrl(), getPrimaryCss(), getCssMode()
 * ✅ Lógica existente 100% preservada
 */

import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.scopes.ScopeType;
import com.ossobo.nexusfx.resources.descriptor.ViewDescriptor;
import com.ossobo.nexusfx.view.design.StyleDefinition;
import com.ossobo.nexusfx.view.design.StyleDefinition.CssMode;
import com.ossobo.nexusfx.view.loader.FXMLService;
import com.ossobo.nexusfx.view.refresh.RefreshManager;
import com.ossobo.nexusfx.view.refresh.RefreshableController;
import com.ossobo.nexusfx.view.registry.ViewRegistry;
import com.ossobo.nexusfx.view.views.LoadedView;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@ScopeAnnotation(ScopeType.SINGLETON)
public final class ViewManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewManager.class);

    private static volatile ViewManager instance;

    public static ViewManager getInstance() {
        if (instance == null) {
            synchronized (ViewManager.class) {
                if (instance == null) {
                    instance = new ViewManager();
                }
            }
        }
        return instance;
    }

    private final ViewRegistry viewRegistry;
    private final FXMLService fxmlService;
    private final RefreshManager refreshManager;

    private final Map<String, LoadedView<?>> viewCache = new ConcurrentHashMap<>();

    private int cacheHits = 0;
    private int cacheMisses = 0;
    private int totalRequests = 0;
    private int dialogRequests = 0;

    private ViewManager() {
        this.viewRegistry = ViewRegistry.getInstance();
        this.fxmlService = new FXMLService();
        this.refreshManager = new RefreshManager();

        LOGGER.info("🚀 ViewManager v8.4 - Adaptado ao ViewDescriptor (resources.descriptor)");
    }

    // ==================== API PÚBLICA - VIEWS NORMAIS ====================

    public LoadedView<Object> loadView(String viewId) {
        return loadView(viewId, Object.class, null);
    }

    public <T> LoadedView<T> loadView(String viewId, Class<T> controllerType) {
        return loadView(viewId, controllerType, null);
    }

    public <T> LoadedView<T> loadView(String viewId, Class<T> controllerType, String customCssPath) {
        validateViewId(viewId);
        totalRequests++;

        @SuppressWarnings("unchecked")
        LoadedView<T> cached = (LoadedView<T>) viewCache.get(viewId);
        if (cached != null) {
            cacheHits++;
            LOGGER.debug("✅ CACHE HIT: {}", viewId);
            return cached;
        }

        cacheMisses++;
        LOGGER.debug("🔄 CACHE MISS: {} (criando nova)", viewId);

        ViewDescriptor descriptor = viewRegistry.getViewDescriptor(viewId);
        LoadedView<T> loadedView = fxmlService.load(descriptor, controllerType);

        applyStylesViaStyleDefinition(loadedView.getRoot(), descriptor, customCssPath);

        viewCache.put(viewId, loadedView);
        registerForRefreshIfDynamic(viewId, loadedView, descriptor);

        return loadedView;
    }

    // ==================== DIÁLOGOS (ALWAYS-FRESH) ====================

    public LoadedView<Object> loadFreshView(String viewId) {
        return loadFreshViewInternal(viewId, Object.class, null, null);
    }

    @SuppressWarnings("unchecked")
    public <T> LoadedView<T> loadFreshView(String viewId, Consumer<T> configurator) {
        return (LoadedView<T>) loadFreshViewInternal(viewId, Object.class, null,
                (Consumer<Object>) configurator);
    }

    public <T> LoadedView<T> loadFreshView(String viewId, Class<T> controllerType,
                                           Consumer<T> configurator) {
        return loadFreshViewInternal(viewId, controllerType, null, configurator);
    }

    private <T> LoadedView<T> loadFreshViewInternal(String viewId, Class<T> controllerType,
                                                    String customCssPath, Consumer<T> configurator) {
        validateViewId(viewId);
        dialogRequests++;

        LOGGER.debug("💬 Diálogo (always-fresh): {}", viewId);

        ViewDescriptor descriptor = viewRegistry.getViewDescriptor(viewId);
        LoadedView<T> loadedView = fxmlService.loadFresh(descriptor, controllerType, configurator);

        applyStylesViaStyleDefinition(loadedView.getRoot(), descriptor, customCssPath);

        LOGGER.info("✅ Diálogo criado (fresh): {}", viewId);
        return loadedView;
    }

    // ==================== FLUXO DE ESTILOS (ADAPTADO) ====================

    /**
     * ✅ ADAPTADO: Usa métodos corretos do ViewDescriptor
     * - descriptor.getId() em vez de getViewId()
     * - descriptor.getPrimaryCss() em vez de getCssPath()
     */
    private void applyStylesViaStyleDefinition(Parent root, ViewDescriptor descriptor,
                                               String customCssPath) {
        try {
            // ✅ USA getPrimaryCss() em vez de getCssPath()
            String cssPath = customCssPath != null ? customCssPath :
                    (descriptor.getPrimaryCss() != null ? descriptor.getPrimaryCss().toString() : null);

            // ✅ USA getCssMode() que retorna ViewDescriptor.CssMode
            CssMode mode = mapToCssMode(descriptor.getCssMode());

            StyleDefinition.Builder builder = StyleDefinition.create();

            if (cssPath != null && !cssPath.isEmpty()) {
                builder.withCustomCss(cssPath);
            }

            builder.withMode(mode)
                    .build()
                    .applyTo(root);

            // ✅ USA getId() em vez de getViewId()
            LOGGER.debug("🎨 Estilos aplicados via StyleDefinition: {} (modo: {})",
                    descriptor.getId(), mode);

        } catch (Exception e) {
            LOGGER.warn("⚠️ Falha ao aplicar estilos via StyleDefinition: {}", e.getMessage());
            applyFallbackStyles(root);
        }
    }

    // ==================== API PÚBLICA DE ESTILOS ====================

    public void applyDesignSystemToHierarchy(Parent root) {
        if (root == null) {
            throw new IllegalArgumentException("Root não pode ser nulo");
        }

        LOGGER.debug("🎨 Aplicando DesignSystem à hierarquia via ViewManager");

        // ✅ Usa construtor correto do ViewDescriptor
        ViewDescriptor globalDescriptor = new ViewDescriptor(
                "__global_styling__",
                null,
                Object.class,
                null,
                null,
                ViewDescriptor.CssMode.NONE,
                ViewDescriptor.ViewType.STATIC,
                com.ossobo.nexusfx.resources.enums.ResourceOrigin.FRAMEWORK
        );

        applyStylesViaStyleDefinition(root, globalDescriptor, null);
    }

    public void applyDesignSystemToHierarchy(Parent root, String viewId) {
        if (root == null) {
            throw new IllegalArgumentException("Root não pode ser nulo");
        }

        LOGGER.debug("🎨 Aplicando DesignSystem à view: {}", viewId);

        ViewDescriptor globalDescriptor = new ViewDescriptor(
                viewId != null ? viewId : "__unknown__",
                null,
                Object.class,
                null,
                null,
                ViewDescriptor.CssMode.NONE,
                ViewDescriptor.ViewType.STATIC,
                com.ossobo.nexusfx.resources.enums.ResourceOrigin.FRAMEWORK
        );

        applyStylesViaStyleDefinition(root, globalDescriptor, null);
    }

    // ==================== MAPEAMENTO DE CSS MODE ====================

    /**
     * ✅ ADAPTADO: ViewDescriptor.CssMode é do pacote resources.descriptor
     */
    private CssMode mapToCssMode(ViewDescriptor.CssMode descriptorMode) {
        if (descriptorMode == null) return CssMode.AUTO;

        switch (descriptorMode) {
            case NONE: return CssMode.AUTO;
            case REPLACE: return CssMode.CUSTOM_ONLY;
            case APPEND: return CssMode.AUTO;
            default: return CssMode.AUTO;
        }
    }

    // ==================== MÉTODOS DE COMPATIBILIDADE ====================

    @SuppressWarnings("unchecked")
    public <T> LoadedView<Object> loadDynamicViewWithController(String viewId,
                                                                Consumer<T> controllerConfigurator) {
        validateViewId(viewId);
        removeFromCache(viewId);

        LoadedView<Object> loadedView = loadView(viewId, Object.class);

        if (controllerConfigurator != null && loadedView.getController() != null) {
            try {
                controllerConfigurator.accept((T) loadedView.getController());
            } catch (ClassCastException e) {
                LOGGER.warn("⚠️ Tipo de controlador incompatível para view '{}'", viewId);
            }
        }

        invokeRefreshIfRefreshable(loadedView.getController(), viewId);
        return loadedView;
    }

    public Parent loadDynamicView(String viewId) {
        LoadedView<?> loaded = loadView(viewId);
        invokeRefreshIfRefreshable(loaded.getController(), viewId);
        return loaded.getRoot();
    }

    public Parent loadStaticView(String viewId) {
        return loadView(viewId).getRoot();
    }

    @SuppressWarnings("unchecked")
    public <T> LoadedView<T> loadStaticViewWithController(String viewId) {
        return (LoadedView<T>) loadView(viewId);
    }

    // ==================== GESTÃO DE CACHE ====================

    public void clearCache() {
        int size = viewCache.size();
        viewCache.clear();
        LOGGER.info("🗑️ Cache limpo: {} entradas", size);
    }

    public void removeFromCache(String viewId) {
        if (viewCache.remove(viewId) != null) {
            LOGGER.debug("🗑️ View removida do cache: {}", viewId);
        }
    }

    public <T> LoadedView<T> reloadView(String viewId, Class<T> controllerType) {
        LOGGER.info("🔄 Recarregando view: {}", viewId);
        removeFromCache(viewId);
        return loadView(viewId, controllerType);
    }

    // ==================== REFRESH MANAGEMENT ====================

    private void registerForRefreshIfDynamic(String viewId, LoadedView<?> loadedView,
                                             ViewDescriptor descriptor) {
        // ✅ USA getViewType() em vez de isDynamic()
        if (descriptor.getViewType() == ViewDescriptor.ViewType.DYNAMIC
                && loadedView.getController() != null) {
            refreshManager.register(viewId, loadedView.getRoot(), loadedView.getController());
            LOGGER.debug("🔄 View dinâmica registrada para refresh: {}", viewId);
        }
    }

    private void invokeRefreshIfRefreshable(Object controller, String viewId) {
        if (controller instanceof RefreshableController) {
            try {
                ((RefreshableController) controller).refreshData();
                LOGGER.debug("🔄 Refresh invocado em: {}", viewId);
            } catch (Exception e) {
                LOGGER.warn("⚠️ Falha ao fazer refresh da view '{}': {}", viewId, e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    public void detachViewFromScene(String viewId, Parent root) {
        try {
            if (root.getScene() != null) {
                root.getScene().setRoot(new Pane());
            }
            viewCache.entrySet().removeIf(entry ->
                    entry.getKey().equals(viewId) && entry.getValue().getRoot() == root);
            LOGGER.debug("📤 View '{}' dissociada da cena", viewId);
        } catch (Exception e) {
            LOGGER.warn("⚠️ Erro ao dissociar view '{}': {}", viewId, e.getMessage());
        }
    }

    public <T> LoadedView<T> configure(LoadedView<T> loadedView, Consumer<T> configurator) {
        if (configurator != null && loadedView.getController() != null) {
            configurator.accept(loadedView.getController());
        }
        return loadedView;
    }

    private void applyFallbackStyles(Parent root) {
        try {
            root.getStylesheets().clear();
            String fallback = "/com/ossobo/nexusfx/styles/fallback.css";
            if (getClass().getResource(fallback) != null) {
                root.getStylesheets().add(getClass().getResource(fallback).toExternalForm());
            }
        } catch (Exception e) {
            // Silêncio
        }
    }

    // ==================== VALIDAÇÃO ====================

    private void validateViewId(String viewId) {
        if (viewId == null || viewId.trim().isEmpty()) {
            throw new IllegalArgumentException("View ID não pode ser nulo ou vazio");
        }
        if (!viewRegistry.isRegistered(viewId)) {
            throw new IllegalArgumentException("View não registrada: " + viewId);
        }
    }

    // ==================== MÉTODOS ESTÁTICOS ====================

    public static LoadedView<Object> load(String viewId) {
        return getInstance().loadView(viewId);
    }

    public static <T> LoadedView<T> load(String viewId, Class<T> controllerType) {
        return getInstance().loadView(viewId, controllerType);
    }

    public static Parent loadParent(String viewId) {
        return getInstance().loadView(viewId).getRoot();
    }

    public static LoadedView<Object> loadFresh(String viewId) {
        return getInstance().loadFreshView(viewId);
    }

    public static <T> LoadedView<T> loadFresh(String viewId, Consumer<T> configurator) {
        return getInstance().loadFreshView(viewId, configurator);
    }

    public static <T> LoadedView<T> loadFresh(String viewId, Class<T> controllerType,
                                              Consumer<T> configurator) {
        return getInstance().loadFreshView(viewId, controllerType, configurator);
    }

    public static <T> LoadedView<Object> loadDynamicWithController(String viewId,
                                                                   Consumer<T> configurator) {
        return getInstance().loadDynamicViewWithController(viewId, configurator);
    }

    public static Parent loadDynamic(String viewId) {
        return getInstance().loadDynamicView(viewId);
    }

    public static Parent loadStatic(String viewId) {
        return getInstance().loadStaticView(viewId);
    }

    public static <T> LoadedView<T> loadStaticWithController(String viewId) {
        return getInstance().loadStaticViewWithController(viewId);
    }

    public static void purgeCache() {
        getInstance().clearCache();
    }

    public static void detachFromScene(String viewId, Parent root) {
        getInstance().detachViewFromScene(viewId, root);
    }

    // ==================== GETTERS ====================

    public boolean isCached(String viewId) {
        return viewCache.containsKey(viewId);
    }

    public int getCacheSize() {
        return viewCache.size();
    }

    public double getCacheHitRate() {
        int total = cacheHits + cacheMisses;
        return total > 0 ? (cacheHits * 100.0 / total) : 0;
    }
}
