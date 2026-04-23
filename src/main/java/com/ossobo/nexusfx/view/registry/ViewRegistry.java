package com.ossobo.nexusfx.view.registry;

import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.descriptor.ViewDescriptor;
import com.ossobo.nexusfx.resources.enums.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * 🎯 VIEW REGISTRY - ADAPTADO AO RESOURCE API ATUAL
 *
 * v3.3 (22/04/2026):
 * - ✅ Usa ViewDescriptor correto (resources.descriptor)
 * - ✅ Mantém registro local para compatibilidade
 * - ✅ Prioriza ResourceAPI, fallback para local
 */
public final class ViewRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewRegistry.class);

    private static final ViewRegistry INSTANCE = new ViewRegistry();

    // Registro local (compatibilidade) - usando o ViewDescriptor correto
    private final Map<String, ViewDescriptor> localRegistry = new HashMap<>();

    // ResourceAPI (datacenter)
    private ResourceAPI resourceAPI;

    private ViewRegistry() {
        LOGGER.info("ViewRegistry v3.3 inicializado - integrado com ResourceAPI");
    }

    public static ViewRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * ✅ Víncula ResourceAPI ao registry
     */
    public void setResourceAPI(ResourceAPI resourceAPI) {
        this.resourceAPI = resourceAPI;
        LOGGER.info("✅ ResourceAPI vinculado ao ViewRegistry. Recursos disponíveis: {}",
                resourceAPI != null ? resourceAPI.count() : 0);
    }

    /**
     * ✅ Verifica se ResourceAPI está disponível
     */
    public boolean isResourceAPIAvailable() {
        return resourceAPI != null;
    }

    // ==================== REGISTRO LOCAL (COMPATIBILIDADE) ====================

    public void registerView(ViewDescriptor descriptor) {
        String viewId = descriptor.getId(); // ✅ getId() em vez de getViewId()

        if (viewId == null || viewId.trim().isEmpty()) {
            throw new IllegalArgumentException("View ID cannot be null or empty");
        }

        if (localRegistry.containsKey(viewId)) {
            throw new IllegalStateException("Duplicate View ID: " + viewId);
        }

        localRegistry.put(viewId, descriptor);
        LOGGER.info("✅ View registrada localmente: {}", viewId);
    }

    // ==================== ACESSO VIA RESOURCE API (DATACENTER) ====================

    /**
     * ✅ Obtém URL do FXML (prioriza ResourceAPI)
     */
    public URL getFxmlUrl(String viewId) {
        // 1. Tenta ResourceAPI
        if (resourceAPI != null && resourceAPI.exists(viewId, ResourceType.FXML)) {
            try {
                return resourceAPI.getViewUrl(viewId);
            } catch (Exception e) {
                LOGGER.warn("⚠️ Falha ao obter URL do ResourceAPI para '{}': {}", viewId, e.getMessage());
            }
        }

        // 2. Fallback para registro local
        if (localRegistry.containsKey(viewId)) {
            ViewDescriptor descriptor = localRegistry.get(viewId);
            URL url = descriptor.getFxmlUrl(); // ✅ getFxmlUrl() retorna URL diretamente
            if (url != null) {
                LOGGER.debug("📁 URL obtida do registro local: {}", viewId);
                return url;
            }
        }

        throw new IllegalArgumentException("View não encontrada: " + viewId);
    }

    /**
     * ✅ Obtém ViewDescriptor do datacenter
     */
    public Optional<ViewDescriptor> getResourceDescriptor(String viewId) {
        if (resourceAPI == null) {
            return Optional.empty();
        }
        return resourceAPI.getViewDescriptor(viewId);
    }

    /**
     * ✅ Obtém URLs de CSS via ResourceAPI
     */
    public List<URL> getCssUrls(String viewId) {
        List<URL> urls = new ArrayList<>();

        if (resourceAPI != null) {
            resourceAPI.getViewDescriptor(viewId).ifPresent(descriptor -> {
                if (descriptor.getPrimaryCss() != null) {
                    urls.add(descriptor.getPrimaryCss());
                }
                urls.addAll(descriptor.getAdditionalCss());
            });
        }

        // Fallback para registro local
        if (urls.isEmpty() && localRegistry.containsKey(viewId)) {
            ViewDescriptor local = localRegistry.get(viewId);
            if (local.getPrimaryCss() != null) {
                urls.add(local.getPrimaryCss());
            }
            urls.addAll(local.getAdditionalCss());
        }

        return urls;
    }

    /**
     * ✅ Obtém Controller Class (prioriza ResourceAPI)
     */
    public Class<?> getControllerClass(String viewId) {
        // 1. ResourceAPI
        if (resourceAPI != null) {
            Optional<ViewDescriptor> opt = resourceAPI.getViewDescriptor(viewId);
            if (opt.isPresent() && opt.get().getControllerClass() != null) {
                return opt.get().getControllerClass();
            }
        }

        // 2. Registro local
        if (localRegistry.containsKey(viewId)) {
            return localRegistry.get(viewId).getControllerClass();
        }

        throw new IllegalArgumentException("View não encontrada: " + viewId);
    }

    // ==================== MÉTODOS DE COMPATIBILIDADE ====================

    public ViewDescriptor getViewDescriptor(String viewId) {
        // 1. Tenta ResourceAPI
        if (resourceAPI != null) {
            Optional<ViewDescriptor> opt = resourceAPI.getViewDescriptor(viewId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        // 2. Registro local
        ViewDescriptor descriptor = localRegistry.get(viewId);
        if (descriptor == null) {
            String errorMsg = "View não registrada: " + viewId +
                    "\nViews disponíveis: " + getRegisteredViews();
            LOGGER.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        return descriptor;
    }

    public String getFxmlPath(String viewId) {
        URL url = getFxmlUrl(viewId);
        return url.toString();
    }

    public String getCssPath(String viewId) {
        ViewDescriptor descriptor = getViewDescriptor(viewId);
        return descriptor.getPrimaryCss() != null ? descriptor.getPrimaryCss().toString() : null;
    }

    public String[] getCssBase(String viewId) {
        ViewDescriptor descriptor = getViewDescriptor(viewId);
        List<URL> additionalCss = descriptor.getAdditionalCss();
        return additionalCss.stream()
                .map(URL::toString)
                .toArray(String[]::new);
    }

    public String[] getCssComponentes(String viewId) {
        // No novo modelo, additionalCss já contém todos os CSS
        return getCssBase(viewId);
    }

    public ViewDescriptor.ViewType getViewType(String viewId) {
        return getViewDescriptor(viewId).getViewType();
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    public boolean isRegistered(String viewId) {
        boolean inResource = resourceAPI != null && resourceAPI.exists(viewId, ResourceType.FXML);
        boolean inLocal = localRegistry.containsKey(viewId);
        return inResource || inLocal;
    }

    public Set<String> getRegisteredViews() {
        Set<String> views = new HashSet<>(localRegistry.keySet());
        if (resourceAPI != null) {
            views.addAll(resourceAPI.listIdsByType(ResourceType.FXML));
        }
        return Collections.unmodifiableSet(views);
    }

    public boolean hasCss(String viewId) {
        try {
            ViewDescriptor descriptor = getViewDescriptor(viewId);
            return descriptor.getPrimaryCss() != null || !descriptor.getAdditionalCss().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDynamic(String viewId) {
        try {
            return getViewDescriptor(viewId).getViewType() == ViewDescriptor.ViewType.DYNAMIC;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isStatic(String viewId) {
        try {
            return getViewDescriptor(viewId).getViewType() == ViewDescriptor.ViewType.STATIC;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== MÉTODOS DE GESTÃO ====================

    public void clearRegistry() {
        int size = localRegistry.size();
        localRegistry.clear();
        LOGGER.info("🗑️ Registry local limpo: {} views removidas", size);
    }

    public int getRegistrySize() {
        return localRegistry.size();
    }

    public void registerAll(ViewDescriptor... descriptors) {
        if (descriptors == null) return;

        LOGGER.info("📦 Registrando {} views em massa", descriptors.length);

        int success = 0;
        int failed = 0;

        for (ViewDescriptor descriptor : descriptors) {
            try {
                registerView(descriptor);
                success++;
            } catch (Exception e) {
                LOGGER.error("❌ Falha ao registrar view {}: {}",
                        descriptor.getId(), e.getMessage());
                failed++;
            }
        }

        LOGGER.info("✅ Registro em massa concluído: {} sucessos, {} falhas", success, failed);
    }

    // ==================== DIAGNÓSTICO ====================

    public void diagnose() {
        System.out.println("\n🔍 VIEW REGISTRY DIAGNÓSTICO v3.3");
        System.out.println("=".repeat(50));
        System.out.println("📊 ESTATÍSTICAS:");
        System.out.println("• ResourceAPI: " + (resourceAPI != null ? "✅ Vinculado" : "❌ Não vinculado"));
        System.out.println("• Views no ResourceAPI: " + (resourceAPI != null ?
                resourceAPI.listIdsByType(ResourceType.FXML).size() : 0));
        System.out.println("• Views no registro local: " + localRegistry.size());
        System.out.println("• Total de views: " + getRegisteredViews().size());
        System.out.println("=".repeat(50));
    }
}
