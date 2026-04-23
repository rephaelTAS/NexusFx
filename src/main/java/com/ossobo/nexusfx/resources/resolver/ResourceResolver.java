/*
 * ResourceResolver v1.0
 *
 * Serviço de resolução de recursos.
 * Responsabilidade: localizar e entregar o recurso bruto já registrado.
 * Entrada: id, tipo opcional, origem opcional.
 * Saída: URL, InputStream, ResourceDescriptor.
 * Depende de: ResourceRegistry.
 */

package com.ossobo.nexusfx.resources.resolver;

import com.ossobo.nexusfx.resources.descriptor.ResourceDescriptor;
import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;
import com.ossobo.nexusfx.resources.excecoes.ResourceNotFoundException;
import com.ossobo.nexusfx.resources.registry.ResourceRegistry;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 🔍 ResourceResolver v1.0
 * <p>
 * Camada de serviço para obtenção de recursos.
 * Transforma metadado em acesso prático sem interpretar o conteúdo.
 * É a ponte entre o catálogo e o uso real.
 * </p>
 *
 * <pre>
 * Uso típico:
 *   URL url = resolver.resolveUrl("login-view");
 *   Optional&lt;ViewDescriptor&gt; view = resolver.resolveTyped("main", ViewDescriptor.class);
 * </pre>
 */
public final class ResourceResolver {

    private static final Logger LOGGER = Logger.getLogger(ResourceResolver.class.getName());

    private final ResourceRegistry registry;

    /**
     * Construtor que recebe o registry como dependência.
     */
    public ResourceResolver(ResourceRegistry registry) {
        this.registry = registry;
        LOGGER.info("🔍 ResourceResolver v1.0 inicializado");
    }

    // ===== RESOLUÇÃO DE URL (MÉTODO PRINCIPAL) =====

    /**
     * Obtém a URL de um recurso registrado.
     *
     * @param id Identificador único do recurso
     * @return URL do recurso
     * @throws ResourceNotFoundException Se recurso não encontrado
     */
    public URL resolveUrl(String id) {
        return resolveDescriptor(id)
                .map(ResourceDescriptor::getUrl)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    /**
     * Obtém a URL de um recurso registrado com tipo específico.
     *
     * @param id Identificador único do recurso
     * @param type Tipo esperado do recurso
     * @return URL do recurso
     * @throws ResourceNotFoundException Se recurso não encontrado ou tipo incompatível
     */
    public URL resolveUrl(String id, ResourceType type) {
        return registry.findByIdAndType(id, type)
                .map(ResourceDescriptor::getUrl)
                .orElseThrow(() -> new ResourceNotFoundException(id, type));
    }

    // ===== RESOLUÇÃO DE DESCRIPTOR =====

    /**
     * Busca um descriptor pelo ID.
     *
     * @param id Identificador do recurso
     * @return Optional com o descriptor, vazio se não encontrado
     */
    public Optional<ResourceDescriptor> resolveDescriptor(String id) {
        LOGGER.log(Level.FINE, () -> "Resolvendo recurso: " + id);
        return registry.findById(id);
    }

    /**
     * Busca um descriptor pelo ID e tipo.
     * Valida compatibilidade de tipo automaticamente.
     *
     * @param id Identificador do recurso
     * @param type Tipo esperado
     * @return Optional com o descriptor, vazio se não encontrado ou tipo incompatível
     */
    public Optional<ResourceDescriptor> resolveDescriptor(String id, ResourceType type) {
        return registry.findByIdAndType(id, type);
    }

    /**
     * Busca um descriptor pelo ID, tipo e origem.
     *
     * @param id Identificador do recurso
     * @param type Tipo esperado
     * @param origin Origem desejada
     * @return Optional com o descriptor, vazio se critérios não atendidos
     */
    public Optional<ResourceDescriptor> resolveDescriptor(String id, ResourceType type, ResourceOrigin origin) {
        return registry.findByIdAndType(id, type)
                .filter(descriptor -> descriptor.getOrigin() == origin);
    }

    // ===== RESOLUÇÃO COM CAST AUTOMÁTICO =====

    /**
     * Resolve um descriptor já convertido para o subtipo esperado.
     *
     * @param id Identificador do recurso
     * @param descriptorClass Classe do descriptor esperado (ViewDescriptor, ImageDescription, etc)
     * @param <T> Tipo do descriptor
     * @return Optional com o descriptor tipado
     */
    @SuppressWarnings("unchecked")
    public <T extends ResourceDescriptor> Optional<T> resolveTyped(String id, Class<T> descriptorClass) {
        return resolveDescriptor(id)
                .filter(descriptor -> descriptorClass.isAssignableFrom(descriptor.getClass()))
                .map(descriptor -> (T) descriptor);
    }

    /**
     * Resolve um descriptor com validação de tipo e origem.
     *
     * @param id Identificador do recurso
     * @param descriptorClass Classe do descriptor esperado
     * @param origin Origem desejada
     * @param <T> Tipo do descriptor
     * @return Optional com o descriptor tipado
     */
    @SuppressWarnings("unchecked")
    public <T extends ResourceDescriptor> Optional<T> resolveTyped(String id,
                                                                   Class<T> descriptorClass,
                                                                   ResourceOrigin origin) {
        return resolveDescriptor(id)
                .filter(descriptor -> descriptorClass.isAssignableFrom(descriptor.getClass()))
                .filter(descriptor -> descriptor.getOrigin() == origin)
                .map(descriptor -> (T) descriptor);
    }

    // ===== RESOLUÇÃO DE INPUTSTREAM =====

    /**
     * Abre um InputStream para o recurso.
     * ATENÇÃO: O chamador é responsável por fechar o stream.
     *
     * @param id Identificador do recurso
     * @return InputStream para leitura do recurso
     * @throws ResourceNotFoundException Se recurso não encontrado ou falha de acesso
     */
    public InputStream resolveStream(String id) throws ResourceNotFoundException {
        try {
            URL url = resolveUrl(id);
            return url.openStream();
        } catch (Exception e) {
            throw new ResourceNotFoundException(id, e);
        }
    }

    // ===== LISTAGEM E CONSULTA =====

    /**
     * Lista todos os IDs registrados de um determinado tipo.
     *
     * @param type Tipo de recurso desejado
     * @return Lista imutável de IDs
     */
    public List<String> listIdsByType(ResourceType type) {
        return registry.findAll().stream()
                .filter(descriptor -> descriptor.getType() == type)
                .map(ResourceDescriptor::getId)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os descritores de um determinado tipo.
     *
     * @param type Tipo de recurso desejado
     * @return Lista imutável de descritores
     */
    public List<ResourceDescriptor> listByType(ResourceType type) {
        return registry.findAll().stream()
                .filter(descriptor -> descriptor.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se um recurso existe no catálogo.
     *
     * @param id Identificador do recurso
     * @return true se existir
     */
    public boolean exists(String id) {
        return registry.findById(id).isPresent();
    }

    /**
     * Verifica se um recurso existe e é do tipo especificado.
     *
     * @param id Identificador do recurso
     * @param type Tipo esperado
     * @return true se existir e for do tipo correto
     */
    public boolean exists(String id, ResourceType type) {
        return registry.findByIdAndType(id, type).isPresent();
    }

    // ===== MÉTODOS DE CONVENIÊNCIA (API SIMPLIFICADA) =====

    /**
     * Obtém URL de uma view FXML.
     */
    public URL getViewUrl(String viewId) {
        return resolveUrl(viewId, ResourceType.FXML);
    }

    /**
     * Obtém URL de uma imagem.
     */
    public URL getImageUrl(String imageId) {
        return resolveUrl(imageId, ResourceType.IMAGE);
    }

    /**
     * Obtém URL de um arquivo CSS.
     */
    public URL getCssUrl(String cssId) {
        return resolveUrl(cssId, ResourceType.CSS);
    }

    /**
     * Obtém URL de um arquivo de som.
     */
    public URL getSoundUrl(String soundId) {
        return resolveUrl(soundId, ResourceType.SOUND);
    }

    /**
     * Obtém URL de um alerta.
     */
    public URL getAlertUrl(String alertId) {
        return resolveUrl(alertId, ResourceType.ALERT);
    }

    @Override
    public String toString() {
        return String.format("ResourceResolver[recursos=%d]", registry.count());
    }
}
