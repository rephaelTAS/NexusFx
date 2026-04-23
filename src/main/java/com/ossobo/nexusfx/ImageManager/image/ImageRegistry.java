package com.ossobo.nexusfx.ImageManager.image;

import com.ossobo.nexusfx.di.annotations.Component;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.scopes.ScopeType;
import com.ossobo.nexusfx.resources.api.ResourceAPI;
import com.ossobo.nexusfx.resources.descriptor.ImageDescription;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 IMAGE REGISTRY - ADAPTADO AO RESOURCE API
 *
 * v2.0 (22/04/2026):
 * - ✅ Consome ResourceAPI como datacenter
 * - ✅ Mantém cache local e registro de paths para compatibilidade
 * - ✅ Prioriza ResourceAPI, fallback para local
 */
@Component
@ScopeAnnotation(ScopeType.SINGLETON)
public class ImageRegistry {
    private static final Logger LOGGER = Logger.getLogger(ImageRegistry.class.getName());
    private static final String CONFIG_FILE = "/image_config.properties";

    // Cache local
    private final Map<String, Image> directImageCache = new ConcurrentHashMap<>();
    private final Map<String, String> resourcePathRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<Runnable>> imageLoadCallbacks = new ConcurrentHashMap<>();

    // ✅ NOVO: ResourceAPI (datacenter)
    private ResourceAPI resourceAPI;

    public ImageRegistry() {
        LOGGER.config("ImageRegistry v2.0 instance created by DIContainer.");
        loadConfigurationsFromFile();
    }

    // ==================== INTEGRAÇÃO COM RESOURCE API ====================

    /**
     * ✅ Víncula ResourceAPI ao registry
     */
    public void setResourceAPI(ResourceAPI resourceAPI) {
        this.resourceAPI = resourceAPI;
        LOGGER.info("✅ ResourceAPI vinculado ao ImageRegistry. Imagens disponíveis: " +
                (resourceAPI != null ? resourceAPI.countByType(ResourceType.IMAGE) : 0));
    }

    /**
     * ✅ Verifica se ResourceAPI está disponível
     */
    public boolean isResourceAPIAvailable() {
        return resourceAPI != null;
    }

    /**
     * ✅ Obtém URL da imagem via ResourceAPI
     */
    public Optional<URL> getImageUrl(String key) {
        if (resourceAPI != null && resourceAPI.exists(key, ResourceType.IMAGE)) {
            try {
                return Optional.of(resourceAPI.getImageUrl(key));
            } catch (Exception e) {
                LOGGER.warning("⚠️ Falha ao obter URL do ResourceAPI para '" + key + "': " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * ✅ Obtém ImageDescription do datacenter
     */
    public Optional<ImageDescription> getImageDescription(String key) {
        if (resourceAPI == null) {
            return Optional.empty();
        }
        return resourceAPI.getImageDescriptor(key);
    }

    /**
     * ✅ Registra imagem no ResourceAPI
     */
    public void registerInResourceAPI(String key, URL imageUrl, ImageDescription.ImageType type) {
        if (resourceAPI == null) {
            LOGGER.warning("ResourceAPI não vinculado. Imagem não registrada no datacenter: " + key);
            return;
        }

        ImageDescription descriptor = new ImageDescription(
                key, imageUrl, type,
                0, 0, true, true, null,
                com.ossobo.nexusfx.resources.enums.ResourceOrigin.APPLICATION
        );
        resourceAPI.register(descriptor);
        LOGGER.info("✅ Imagem registrada no ResourceAPI: " + key);
    }

    // --- Métodos de Carregamento e Registro ---

    /**
     * Carrega os mapeamentos de imagens a partir do arquivo de propriedades.
     */
    private void loadConfigurationsFromFile() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.warning("Arquivo de configuração de imagem '" + CONFIG_FILE + "' NÃO ENCONTRADO.");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            for (String key : prop.stringPropertyNames()) {
                String path = prop.getProperty(key);
                if (path != null && !path.trim().isEmpty()) {
                    registerImage(key, path.trim());
                }
            }
            LOGGER.info("Total de " + prop.size() + " imagens carregadas do arquivo de configuração.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar arquivo de configuração.", e);
        }
    }

    /**
     * Registra uma imagem diretamente com uma chave única.
     */
    public void registerImage(String key, Image image) {
        validateKey(key);
        Objects.requireNonNull(image, "Image cannot be null");
        checkIfKeyAlreadyExists(key);
        directImageCache.put(key, image);
        LOGGER.log(Level.FINE, "Direct image registered with key: {0}", key);
        notifyCallbacks(key, image);
    }

    /**
     * Registra um caminho de recurso para carregamento posterior.
     */
    public void registerImage(String key, String resourcePath) {
        validateKey(key);
        Objects.requireNonNull(resourcePath, "Resource path cannot be null");

        // Validação do recurso
        if (getClass().getResource(resourcePath) == null) {
            LOGGER.warning("Recurso de imagem NÃO ENCONTRADO para chave '" + key + "' no caminho '" + resourcePath + "'.");
            return;
        }

        checkIfKeyAlreadyExists(key);
        resourcePathRegistry.put(key, resourcePath);
        LOGGER.log(Level.FINE, "Resource path registered with key: {0}", key);
    }

    // --- Métodos de Leitura e Consulta ---

    /**
     * ✅ Obtém uma imagem registrada (prioriza ResourceAPI)
     */
    public Optional<Image> getImage(String key) {
        // 1. Cache local
        Image directImage = directImageCache.get(key);
        if (directImage != null) {
            return Optional.of(directImage);
        }

        // 2. ResourceAPI (datacenter)
        Optional<URL> urlOpt = getImageUrl(key);
        if (urlOpt.isPresent()) {
            try {
                Image image = new Image(urlOpt.get().toExternalForm());
                directImageCache.put(key, image); // Cacheia
                notifyCallbacks(key, image);
                return Optional.of(image);
            } catch (Exception e) {
                LOGGER.warning("Falha ao carregar imagem do ResourceAPI: " + key);
            }
        }

        // 3. Resource path local (fallback)
        return getImagePath(key)
                .map(path -> {
                    try {
                        InputStream stream = getClass().getResourceAsStream(path);
                        if (stream != null) {
                            Image image = new Image(stream);
                            directImageCache.put(key, image);
                            notifyCallbacks(key, image);
                            return image;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to load registered image: " + key, e);
                    }
                    return null;
                });
    }

    /**
     * Versão que lança exceção se a imagem não existir.
     */
    public Image getImageOrThrow(String key) {
        return getImage(key).orElseThrow(() -> {
            String errorMsg = "Image not registered: " + key +
                    "\nAvailable images: " + getRegisteredKeys();
            LOGGER.warning(errorMsg);
            return new IllegalArgumentException(errorMsg);
        });
    }

    /**
     * ✅ Verifica se uma chave está registrada (ResourceAPI + Local)
     */
    public boolean isRegistered(String key) {
        boolean inResource = resourceAPI != null && resourceAPI.exists(key, ResourceType.IMAGE);
        boolean inCache = directImageCache.containsKey(key);
        boolean inPathRegistry = resourcePathRegistry.containsKey(key);
        return inResource || inCache || inPathRegistry;
    }

    /**
     * Obtém o caminho do recurso para uma chave.
     */
    public Optional<String> getImagePath(String key) {
        return Optional.ofNullable(resourcePathRegistry.get(key));
    }

    /**
     * ✅ Obtém todas as chaves registradas (ResourceAPI + Local)
     */
    public Set<String> getRegisteredKeys() {
        Set<String> keys = new HashSet<>();
        keys.addAll(directImageCache.keySet());
        keys.addAll(resourcePathRegistry.keySet());

        // Adiciona imagens do ResourceAPI
        if (resourceAPI != null) {
            keys.addAll(resourceAPI.listIdsByType(ResourceType.IMAGE));
        }

        return Collections.unmodifiableSet(keys);
    }

    // --- Métodos de Utilidade e Limpeza ---

    /**
     * Registro em massa de imagens.
     */
    public void registerAll(Map<String, Image> images) {
        Objects.requireNonNull(images, "Images map cannot be null");
        images.forEach(this::registerImage);
    }

    /**
     * Limpa todo o registro local (ResourceAPI permanece intacto).
     */
    public void clearRegistry() {
        LOGGER.info("Clearing local image registry");
        directImageCache.clear();
        resourcePathRegistry.clear();
        imageLoadCallbacks.clear();
    }

    /**
     * Registra um callback para quando uma imagem estiver disponível.
     */
    public void onImageAvailable(String key, Runnable callback) {
        if (directImageCache.containsKey(key)) {
            callback.run();
        } else {
            imageLoadCallbacks.computeIfAbsent(key, k -> new ArrayList<>()).add(callback);
        }
    }

    /**
     * Registro seguro (se ausente).
     */
    public boolean registerImageIfAbsent(String key, Image image) {
        try {
            validateKey(key);
            Objects.requireNonNull(image, "Image cannot be null");
            return directImageCache.putIfAbsent(key, image) == null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to register image: " + key, e);
            return false;
        }
    }

    // ==================== DIAGNÓSTICO ====================

    /**
     * ✅ Diagnóstico completo do ImageRegistry
     */
    public void diagnose() {
        System.out.println("\n🔍 IMAGE REGISTRY DIAGNÓSTICO v2.0");
        System.out.println("=".repeat(50));
        System.out.println("📊 ESTATÍSTICAS:");
        System.out.println("• ResourceAPI: " + (resourceAPI != null ? "✅ Vinculado" : "❌ Não vinculado"));
        System.out.println("• Imagens no ResourceAPI: " + (resourceAPI != null ?
                resourceAPI.countByType(ResourceType.IMAGE) : 0));
        System.out.println("• Cache local: " + directImageCache.size());
        System.out.println("• Paths registrados: " + resourcePathRegistry.size());
        System.out.println("• Total de chaves: " + getRegisteredKeys().size());
        System.out.println("=".repeat(50));
    }

    // --- Métodos Privados de Validação ---

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Image key cannot be null or empty");
        }
    }

    private void checkIfKeyAlreadyExists(String key) {
        if (directImageCache.containsKey(key) || resourcePathRegistry.containsKey(key)) {
            String errorMsg = "Duplicate Image Key: " + key;
            LOGGER.severe(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    private void notifyCallbacks(String key, Image image) {
        List<Runnable> callbacks = imageLoadCallbacks.remove(key);
        if (callbacks != null) {
            callbacks.forEach(Runnable::run);
        }
    }
}
