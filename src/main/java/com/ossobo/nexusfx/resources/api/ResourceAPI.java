/*
 * ResourceAPI v1.1
 *
 * Responsabilidade: expor métodos simples para os outros módulos.
 * Entrada: id ou tipo de recurso.
 * Saída: acesso rápido e padronizado.
 * Depende de: ResourceRegistry, ResourceResolver, ResourceGuard.
 */

package com.ossobo.nexusfx.resources.api;

import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;

import com.ossobo.nexusfx.di.scopes.enums.ScopeType;
import com.ossobo.nexusfx.resources.descriptor.*;
import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;
import com.ossobo.nexusfx.resources.excecoes.ResourceNotFoundException;
import com.ossobo.nexusfx.resources.excecoes.ResourceValidationException;
import com.ossobo.nexusfx.resources.guard.ResourceGuard;
import com.ossobo.nexusfx.resources.registry.ResourceRegistry;
import com.ossobo.nexusfx.resources.resolver.ResourceResolver;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 🌐 ResourceAPI v1.1
 * <p>
 * Fachada unificada para o módulo de Resources.
 * Ponto único de entrada para todos os consumidores do framework.
 * </p>
 *
 * <pre>
 * Exemplos de uso:
 *   // Views
 *   resourceAPI.registerView(descriptor);
 *   Optional&lt;ViewDescriptor&gt; view = resourceAPI.getViewDescriptor("main");
 *
 *   // Alertas (mesmo descritor, registro separado)
 *   resourceAPI.registerAlert(descriptor);
 *   Optional&lt;ViewDescriptor&gt; alerta = resourceAPI.getAlertDescriptor("fx-alert-critical");
 * </pre>
 */
@Component
@ScopeAnnotation(ScopeType.SINGLETON)
public final class ResourceAPI {

    private static final Logger LOGGER = Logger.getLogger(ResourceAPI.class.getName());

    private final ResourceRegistry registry;
    private final ResourceResolver resolver;
    private final ResourceGuard guard;

    public ResourceAPI() {
        this.registry = new ResourceRegistry();
        this.resolver = new ResourceResolver(registry);
        this.guard = new ResourceGuard(registry);
        LOGGER.info("🌐 ResourceAPI v1.1 inicializada");
    }

    // ===== REGISTRO GENÉRICO =====

    public void register(ResourceDescriptor descriptor) {
        guard.validateForRegistration(descriptor);
        registry.register(descriptor);
        LOGGER.fine(() -> "📝 Recurso registrado: " + descriptor.getId());
    }

    public void registerAll(ResourceDescriptor... descriptors) {
        for (ResourceDescriptor descriptor : descriptors) {
            register(descriptor);
        }
    }

    public void unregister(String id) {
        registry.unregister(id);
        LOGGER.fine(() -> "🗑️ Recurso removido: " + id);
    }

    // ===== REGISTRO DE VIEW =====

    /**
     * Registra uma view FXML.
     * O ViewDescriptor é registrado com ResourceType.FXML.
     */
    public void registerView(ViewDescriptor descriptor) {
        if (descriptor.getControllerClass() == null) {
            throw new ResourceValidationException("View requer controllerClass: " + descriptor.getId());
        }
        register(descriptor);
        LOGGER.fine(() -> "🪟 View registrada: " + descriptor.getId());
    }

    /**
     * Registra uma view de forma simplificada (compatibilidade).
     */
    public void registerView(String id, URL fxmlUrl, Class<?> controllerClass, ResourceOrigin origin) {
        ViewDescriptor descriptor = ViewDescriptor.builder()
                .id(id)
                .fxmlUrl(fxmlUrl)
                .controllerClass(controllerClass)
                .origin(origin)
                .build();
        registerView(descriptor);
    }

    // ===== REGISTRO DE ALERTA =====

    /**
     * Registra um alerta.
     * Usa o mesmo ViewDescriptor, mas validado como ALERT.
     */
    public void registerAlert(ViewDescriptor descriptor) {
        if (descriptor.getAlertType() == null) {
            throw new ResourceValidationException("Alerta requer alertType: " + descriptor.getId());
        }
        if (descriptor.getModality() == null) {
            throw new ResourceValidationException("Alerta requer modality: " + descriptor.getId());
        }
        if (descriptor.getControllerClass() == null) {
            throw new ResourceValidationException("Alerta requer controllerClass: " + descriptor.getId());
        }
        register(descriptor);
        LOGGER.fine(() -> "⚠️ Alerta registrado: " + descriptor.getId()
                + " [" + descriptor.getAlertType() + "]");
    }

    // ===== REGISTRO DE OUTROS TIPOS =====

    public void registerImage(String id, URL imageUrl, ResourceOrigin origin) {
        ImageDescription descriptor = new ImageDescription(
                id, imageUrl, ImageDescription.ImageType.IMAGE,
                0, 0, true, true, null, origin
        );
        register(descriptor);
    }

    // ===== CONSULTA BÁSICA =====

    public Optional<ResourceDescriptor> find(String id) {
        return resolver.resolveDescriptor(id);
    }

    public ResourceDescriptor require(String id) {
        return find(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public boolean exists(String id) {
        return resolver.exists(id);
    }

    public boolean exists(String id, ResourceType type) {
        return resolver.exists(id, type);
    }

    // ===== ACESSO A URLS =====

    public URL getUrl(String id) {
        return resolver.resolveUrl(id);
    }

    public URL getViewUrl(String viewId) {
        return resolver.getViewUrl(viewId);
    }

    public URL getImageUrl(String imageId) {
        return resolver.getImageUrl(imageId);
    }

    public URL getCssUrl(String cssId) {
        return resolver.getCssUrl(cssId);
    }

    public URL getSoundUrl(String soundId) {
        return resolver.getSoundUrl(soundId);
    }

    public URL getAlertUrl(String alertId) {
        return resolver.getAlertUrl(alertId);
    }

    // ===== ACESSO A STREAMS =====

    public InputStream openStream(String id) {
        return resolver.resolveStream(id);
    }

    // ===== DESCRITORES TIPADOS (ViewDescriptor unificado) =====

    /**
     * Obtém um ViewDescriptor pelo ID (view ou alerta).
     */
    public Optional<ViewDescriptor> getView(String id) {
        return resolver.resolveTyped(id, ViewDescriptor.class);
    }

    /**
     * ✅ Obtém um ViewDescriptor de view (filtra por ResourceType.FXML).
     */
    public Optional<ViewDescriptor> getViewDescriptor(String viewId) {
        return resolver.resolveTyped(viewId, ViewDescriptor.class)
                .filter(d -> d.getType() == ResourceType.FXML);
    }

    /**
     * ✅ Obtém um ViewDescriptor de alerta (filtra por ResourceType.ALERT).
     */
    public Optional<ViewDescriptor> getAlertDescriptor(String alertId) {
        return resolver.resolveTyped(alertId, ViewDescriptor.class)
                .filter(d -> d.getType() == ResourceType.ALERT);
    }

    /**
     * Obtém um ViewDescriptor por ID e origem.
     */
    public Optional<ViewDescriptor> getViewDescriptor(String viewId, ResourceOrigin origin) {
        return resolver.resolveTyped(viewId, ViewDescriptor.class, origin);
    }

    public Optional<ImageDescription> getImageDescriptor(String imageId) {
        return resolver.resolveTyped(imageId, ImageDescription.class);
    }

    // ===== LISTAGEM =====

    public List<String> listAllIds() {
        return registry.findAll().stream()
                .map(ResourceDescriptor::getId)
                .toList();
    }

    public List<String> listIdsByType(ResourceType type) {
        return resolver.listIdsByType(type);
    }

    public List<ResourceDescriptor> listByType(ResourceType type) {
        return resolver.listByType(type);
    }

    /**
     * Lista todas as views (FXML) como ViewDescriptor.
     */
    public List<ViewDescriptor> listAllViews() {
        return resolver.listByType(ResourceType.FXML).stream()
                .filter(d -> d instanceof ViewDescriptor)
                .map(d -> (ViewDescriptor) d)
                .toList();
    }

    /**
     * Lista todos os alertas (ALERT) como ViewDescriptor.
     */
    public List<ViewDescriptor> listAllAlerts() {
        return resolver.listByType(ResourceType.ALERT).stream()
                .filter(d -> d instanceof ViewDescriptor)
                .map(d -> (ViewDescriptor) d)
                .toList();
    }

    // ===== ESTATÍSTICAS =====

    public int count() {
        return registry.count();
    }

    public long countByType(ResourceType type) {
        return registry.findAll().stream()
                .filter(d -> d.getType() == type)
                .count();
    }

    public void clear() {
        registry.clear();
        LOGGER.warning("⚠️ Catálogo de recursos completamente limpo");
    }

    @Override
    public String toString() {
        return String.format("ResourceAPI[total=%d, views=%d, alerts=%d, images=%d, css=%d, sounds=%d]",
                count(),
                countByType(ResourceType.FXML),
                countByType(ResourceType.ALERT),
                countByType(ResourceType.IMAGE),
                countByType(ResourceType.CSS),
                countByType(ResourceType.SOUND)
        );
    }
}