/*
 * ResourceAPI v1.0
 *
 * Responsabilidade: expor métodos simples para os outros módulos.
 * Entrada: id ou tipo de recurso.
 * Saída: acesso rápido e padronizado.
 * Depende de: ResourceRegistry, ResourceResolver, ResourceGuard.
 */

package com.ossobo.nexusfx.resources.api;

import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.scopes.ScopeType;
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
 * 🌐 ResourceAPI v1.0
 * <p>
 * Fachada unificada para o módulo de Resources.
 * Ponto único de entrada para todos os consumidores do framework.
 * </p>
 *
 * <pre>
 * Exemplos de uso:
 *   resourceAPI.register(viewDescriptor);
 *   URL url = resourceAPI.getViewUrl("login");
 *   Optional&lt;ViewDescriptor&gt; view = resourceAPI.getViewDescriptor("main");
 * </pre>
 */
@Component
@ScopeAnnotation(ScopeType.SINGLETON)
public final class ResourceAPI {

    private static final Logger LOGGER = Logger.getLogger(ResourceAPI.class.getName());

    private final ResourceRegistry registry;
    private final ResourceResolver resolver;
    private final ResourceGuard guard;

    /**
     * Construtor padrão para injeção de dependência.
     * Inicializa todos os componentes internos.
     */
    public ResourceAPI() {
        this.registry = new ResourceRegistry();
        this.resolver = new ResourceResolver(registry);
        this.guard = new ResourceGuard(registry);
        LOGGER.info("🌐 ResourceAPI v1.0 inicializada");
    }

    // ===== REGISTRO =====

    /**
     * Registra um recurso no catálogo.
     * Valida automaticamente antes de registrar.
     *
     * @param descriptor Descriptor do recurso
     * @throws ResourceValidationException Se validação falhar
     */
    public void register(ResourceDescriptor descriptor) {
        guard.validateForRegistration(descriptor);
        registry.register(descriptor);
        LOGGER.fine(() -> "📝 Recurso registrado: " + descriptor.getId());
    }

    /**
     * Registra múltiplos recursos de uma vez.
     *
     * @param descriptors Array de descritores
     */
    public void registerAll(ResourceDescriptor... descriptors) {
        for (ResourceDescriptor descriptor : descriptors) {
            register(descriptor);
        }
    }

    /**
     * Remove um recurso do catálogo.
     *
     * @param id Identificador do recurso
     */
    public void unregister(String id) {
        registry.unregister(id);
        LOGGER.fine(() -> "🗑️ Recurso removido: " + id);
    }

    // ===== CONSULTA BÁSICA =====

    /**
     * Busca um recurso pelo ID.
     *
     * @param id Identificador do recurso
     * @return Optional com o descriptor
     */
    public Optional<ResourceDescriptor> find(String id) {
        return resolver.resolveDescriptor(id);
    }

    /**
     * Busca um recurso obrigatório (lança exceção se não encontrado).
     *
     * @param id Identificador do recurso
     * @return Descriptor do recurso
     * @throws ResourceNotFoundException Se recurso não encontrado
     */
    public ResourceDescriptor require(String id) {
        return find(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    /**
     * Verifica se um recurso existe.
     *
     * @param id Identificador do recurso
     * @return true se existir
     */
    public boolean exists(String id) {
        return resolver.exists(id);
    }

    /**
     * Verifica se um recurso existe e é do tipo especificado.
     *
     * @param id Identificador do recurso
     * @param type Tipo esperado
     * @return true se existir e for do tipo correto
     */
    public boolean exists(String id, ResourceType type) {
        return resolver.exists(id, type);
    }

    // ===== ACESSO A URLS =====

    /**
     * Obtém a URL de qualquer recurso registrado.
     *
     * @param id Identificador do recurso
     * @return URL do recurso
     * @throws ResourceNotFoundException Se recurso não encontrado
     */
    public URL getUrl(String id) {
        return resolver.resolveUrl(id);
    }

    /**
     * Obtém URL de uma view FXML.
     */
    public URL getViewUrl(String viewId) {
        return resolver.getViewUrl(viewId);
    }

    /**
     * Obtém URL de uma imagem.
     */
    public URL getImageUrl(String imageId) {
        return resolver.getImageUrl(imageId);
    }

    /**
     * Obtém URL de um arquivo CSS.
     */
    public URL getCssUrl(String cssId) {
        return resolver.getCssUrl(cssId);
    }

    /**
     * Obtém URL de um arquivo de som.
     */
    public URL getSoundUrl(String soundId) {
        return resolver.getSoundUrl(soundId);
    }

    /**
     * Obtém URL de um alerta.
     */
    public URL getAlertUrl(String alertId) {
        return resolver.getAlertUrl(alertId);
    }

    // ===== ACESSO A STREAMS =====

    /**
     * Abre um InputStream para o recurso.
     * ATENÇÃO: O chamador é responsável por fechar o stream.
     *
     * @param id Identificador do recurso
     * @return InputStream para leitura
     * @throws ResourceNotFoundException Se recurso não encontrado ou falha de acesso
     */
    public InputStream openStream(String id) {
        return resolver.resolveStream(id);
    }

    // ===== DESCRITORES TIPADOS =====

    /**
     * Obtém um ViewDescriptor pelo ID.
     */
    public Optional<ViewDescriptor> getViewDescriptor(String viewId) {
        return resolver.resolveTyped(viewId, ViewDescriptor.class);
    }

    /**
     * Obtém um ImageDescription pelo ID.
     */
    public Optional<ImageDescription> getImageDescriptor(String imageId) {
        return resolver.resolveTyped(imageId, ImageDescription.class);
    }

    /**
     * Obtém um AlertDescriptor pelo ID.
     */
    public Optional<AlertDescriptor> getAlertDescriptor(String alertId) {
        return resolver.resolveTyped(alertId, AlertDescriptor.class);
    }

    /**
     * Obtém um ViewDescriptor de origem específica.
     */
    public Optional<ViewDescriptor> getViewDescriptor(String viewId, ResourceOrigin origin) {
        return resolver.resolveTyped(viewId, ViewDescriptor.class, origin);
    }

    // ===== LISTAGEM =====

    /**
     * Lista todos os IDs registrados.
     */
    public List<String> listAllIds() {
        return registry.findAll().stream()
                .map(ResourceDescriptor::getId)
                .toList();
    }

    /**
     * Lista IDs de um tipo específico.
     */
    public List<String> listIdsByType(ResourceType type) {
        return resolver.listIdsByType(type);
    }

    /**
     * Lista todos os descritores de um tipo.
     */
    public List<ResourceDescriptor> listByType(ResourceType type) {
        return resolver.listByType(type);
    }

    /**
     * Lista todos os ViewDescriptors registrados.
     */
    public List<ViewDescriptor> listAllViews() {
        return resolver.listByType(ResourceType.FXML).stream()
                .filter(d -> d instanceof ViewDescriptor)
                .map(d -> (ViewDescriptor) d)
                .toList();
    }

    /**
     * Lista todos os AlertDescriptors registrados.
     */
    public List<AlertDescriptor> listAllAlerts() {
        return resolver.listByType(ResourceType.ALERT).stream()
                .filter(d -> d instanceof AlertDescriptor)
                .map(d -> (AlertDescriptor) d)
                .toList();
    }

    // ===== ESTATÍSTICAS =====

    /**
     * Retorna o número total de recursos registrados.
     */
    public int count() {
        return registry.count();
    }

    /**
     * Retorna o número de recursos de um tipo específico.
     */
    public long countByType(ResourceType type) {
        return registry.findAll().stream()
                .filter(d -> d.getType() == type)
                .count();
    }

    /**
     * Limpa todo o catálogo.
     * Use com cuidado!
     */
    public void clear() {
        registry.clear();
        LOGGER.warning("⚠️ Catálogo de recursos completamente limpo");
    }

    // ===== MÉTODOS DE CONVENIÊNCIA =====

    /**
     * Registra uma view de forma simplificada.
     */
    public void registerView(String id, URL fxmlUrl, Class<?> controllerClass, ResourceOrigin origin) {
        ViewDescriptor descriptor = new ViewDescriptor(
                id, fxmlUrl, controllerClass, null, null,
                ViewDescriptor.CssMode.NONE, ViewDescriptor.ViewType.STATIC, origin
        );
        register(descriptor);
    }

    /**
     * Registra uma imagem de forma simplificada.
     */
    public void registerImage(String id, URL imageUrl, ResourceOrigin origin) {
        ImageDescription descriptor = new ImageDescription(
                id, imageUrl, ImageDescription.ImageType.IMAGE,
                0, 0, true, true, null, origin
        );
        register(descriptor);
    }

    @Override
    public String toString() {
        return String.format("ResourceAPI[total=%d, views=%d, images=%d, css=%d, sounds=%d, alerts=%d]",
                count(),
                countByType(ResourceType.FXML),
                countByType(ResourceType.IMAGE),
                countByType(ResourceType.CSS),
                countByType(ResourceType.SOUND),
                countByType(ResourceType.ALERT)
        );
    }
}
