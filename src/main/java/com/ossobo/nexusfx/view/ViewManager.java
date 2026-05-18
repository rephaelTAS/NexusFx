// ViewManager.java v9.0
// Responsabilidade: Gerenciar carregamento de views e alertas
// Fonte única da verdade: ResourceAPI → ResourceRegistry
package com.ossobo.nexusfx.view;

import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.scopes.enums.ScopeType;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.descriptor.ViewDescriptor;
import com.ossobo.nexusfx.view.design.StyleManager;
import com.ossobo.nexusfx.view.loader.FXMLService;
import com.ossobo.nexusfx.view.loader.LoadedView;
import com.ossobo.nexusfx.view.refresh.RefreshManager;
import com.ossobo.nexusfx.view.refresh.RefreshableController;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 🎯 ViewManager v9.0
 *
 * Fonte única da verdade: ResourceAPI → ResourceRegistry.
 * ViewRegistry (obsoleto) removido.
 */
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

    private ResourceAPI resourceAPI;
    private final FXMLService fxmlService;
    private final StyleManager styleManager;
    private final RefreshManager refreshManager;
    private final Map<String, LoadedView<?>> viewCache = new ConcurrentHashMap<>();

    private int cacheHits = 0;
    private int cacheMisses = 0;
    private int totalRequests = 0;
    private int dialogRequests = 0;

    private ViewManager() {
        this.fxmlService = new FXMLService();
        this.styleManager = StyleManager.getInstance();
        this.refreshManager = new RefreshManager();
        LOGGER.info("🚀 ViewManager v9.0 — Fonte: ResourceAPI");
    }

    // ==================== VÍNCULO COM RESOURCE API ====================

    public void setResourceAPI(ResourceAPI api) {
        this.resourceAPI = api;
        LOGGER.info("✅ ResourceAPI vinculado ao ViewManager");
    }

    // ==================== RESOLUÇÃO DE DESCRITORES ====================

    private ViewDescriptor obterView(String viewId) {
        if (resourceAPI == null) {
            throw new IllegalStateException("ResourceAPI não vinculado ao ViewManager");
        }
        return resourceAPI.getViewDescriptor(viewId)
                .orElseThrow(() -> new IllegalArgumentException("View não registrada: " + viewId));
    }

    private ViewDescriptor obterAlerta(String alertId) {
        if (resourceAPI == null) {
            throw new IllegalStateException("ResourceAPI não vinculado ao ViewManager");
        }
        return resourceAPI.getAlertDescriptor(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta não registrado: " + alertId));
    }

    // ==================== API PÚBLICA — VIEWS (COM CACHE) ====================

    public LoadedView<Object> loadView(String viewId) {
        return loadView(viewId, Object.class);
    }

    public <T> LoadedView<T> loadView(String viewId, Class<T> controllerType) {
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

        ViewDescriptor descriptor = obterView(viewId);
        LoadedView<T> loadedView = fxmlService.load(descriptor, controllerType);
        styleManager.apply(loadedView.getRoot(), descriptor);

        viewCache.put(viewId, loadedView);
        registerForRefreshIfDynamic(viewId, loadedView, descriptor);

        return loadedView;
    }

    // ==================== API PÚBLICA — ALERTAS (ALWAYS-FRESH) ====================

    public <T> LoadedView<T> loadAlert(String alertId, Class<T> controllerType) {
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert ID não pode ser nulo ou vazio");
        }
        dialogRequests++;
        LOGGER.debug("⚠️ Carregando alerta: {}", alertId);

        ViewDescriptor descriptor = obterAlerta(alertId);
        LoadedView<T> loadedView = fxmlService.loadFresh(descriptor, controllerType, null);
        styleManager.apply(loadedView.getRoot(), descriptor);

        LOGGER.info("✅ Alerta carregado: {}", alertId);
        return loadedView;
    }

    // ==================== API PÚBLICA — DIÁLOGOS (ALWAYS-FRESH) ====================

    public LoadedView<Object> loadFreshView(String viewId) {
        return loadFreshView(viewId, Object.class, null);
    }

    @SuppressWarnings("unchecked")
    public <T> LoadedView<T> loadFreshView(String viewId, Consumer<T> configurator) {
        return (LoadedView<T>) loadFreshViewInternal(viewId, (Class<T>) Object.class, configurator);
    }

    public <T> LoadedView<T> loadFreshView(String viewId, Class<T> controllerType,
                                           Consumer<T> configurator) {
        return loadFreshViewInternal(viewId, controllerType, configurator);
    }

    private <T> LoadedView<T> loadFreshViewInternal(String viewId, Class<T> controllerType,
                                                    Consumer<T> configurator) {
        dialogRequests++;
        LOGGER.debug("💬 Diálogo (always-fresh): {}", viewId);

        ViewDescriptor descriptor = obterView(viewId);
        LoadedView<T> loadedView = fxmlService.loadFresh(descriptor, controllerType, configurator);
        styleManager.apply(loadedView.getRoot(), descriptor);

        LOGGER.info("✅ Diálogo criado (fresh): {}", viewId);
        return loadedView;
    }

    // ==================== MÉTODOS DE COMPATIBILIDADE ====================

    @SuppressWarnings("unchecked")
    public <T> LoadedView<Object> loadDynamicViewWithController(String viewId,
                                                                Consumer<T> controllerConfigurator) {
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
        if (descriptor.getViewType() == ViewDescriptor.ViewType.DYNAMIC
                && loadedView.getController() != null) {
            refreshManager.register(viewId, loadedView.getRoot(), loadedView.getController());
            LOGGER.debug("🔄 View dinâmica registrada para refresh: {}", viewId);
        }
    }

    private void invokeRefreshIfRefreshable(Object controller, String viewId) {
        if (controller instanceof RefreshableController refreshable) {
            try {
                refreshable.refreshData();
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