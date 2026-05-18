package com.ossobo.nexusfx.ImageManager;

import com.ossobo.nexusfx.ImageManager.image.ImageRegistry;
import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.annotations.Service;
import com.ossobo.nexusfx.di.scopes.enums.ScopeType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎯 IMAGE SERVICE COORDINATOR - Orquestra componentes especializados
 * Design Pattern: Facade + Service Layer
 *
 * 🔥 API UNIFICADA SIMPLIFICADA:
 * - load(Stage, String)           ← Ícone para Stage
 * - load(ImageView, String)       ← Imagem para ImageView
 * - load(Label, String)           ← Ícone para Label
 * - load(ImageView, String, w, h) ← Imagem com tamanho
 * - loadImage(String)             ← Retorna Image para uso genérico
 * - clearImageCache()             ← Limpa o cache
 *
 * @author Rafael Tavares
 * @since 2.0
 */
@Service
@ScopeAnnotation(ScopeType.SINGLETON)
public class ImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final Map<String, SoftReference<Image>> imageCache = new ConcurrentHashMap<>();

    // ✅ ALTERADO: não é mais final para permitir injeção pelo bootstrap
    private ImageRegistry imageRegistry;

    private int maxCacheSize = 200;

    /**
     * Construtor padrão (cria ImageRegistry interno)
     */
    public ImageService() {
        this.imageRegistry = new ImageRegistry();
        LOGGER.info("🖼️ ImageService criado com ImageRegistry interno");
    }

    /**
     * ✅ Vincula o ImageRegistry externo (chamado pelo bootstrap)
     * Substitui o ImageRegistry interno pelo que foi inicializado no bootstrap
     */
    public void setImageRegistry(ImageRegistry imageRegistry) {
        if (imageRegistry != null) {
            this.imageRegistry = imageRegistry;
            LOGGER.info("✅ ImageRegistry externo vinculado ao ImageService");
        }
    }

    /**
     * Retorna o ImageRegistry atual
     */
    public ImageRegistry getImageRegistry() {
        return imageRegistry;
    }

    public enum Source {
        CONFIG,
        FILE,
        URL,
        RESOURCE
    }

    // ===== 🎯 API UNIFICADA (PRINCIPAL) =====

    /**
     * ✅ CARREGA IMAGEM PARA STAGE (ícone da janela)
     * Uso: NexusFX.images().load(stage, AppImageConfig.System.APP_LOGO);
     */
    public void load(Stage stage, String imageKey) {
        if (stage == null || imageKey == null || imageKey.trim().isEmpty()) {
            LOGGER.warn("Parâmetros inválidos para load(Stage): stage={}, key={}", stage, imageKey);
            return;
        }

        try {
            Image image = loadImage(imageKey);
            if (image != null) {
                stage.getIcons().add(image);
                LOGGER.debug("Ícone aplicado ao Stage: {}", imageKey);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao aplicar ícone no Stage: {}", imageKey, e);
        }
    }

    /**
     * ✅ CARREGA IMAGEM PARA IMAGEVIEW
     * Uso: NexusFX.images().load(imageView, AppImageConfig.System.SETTING);
     */
    public void load(ImageView target, String imageKey) {
        if (target == null || imageKey == null || imageKey.trim().isEmpty()) {
            LOGGER.warn("Parâmetros inválidos para load(ImageView): target={}, key={}", target, imageKey);
            return;
        }

        try {
            Image image = loadImage(imageKey);
            if (image != null) {
                target.setImage(image);
                LOGGER.trace("Imagem aplicada ao ImageView: {}", imageKey);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao aplicar imagem no ImageView: {}", imageKey, e);
        }
    }

    /**
     * ✅ CARREGA ÍCONE PARA LABEL
     * Uso: NexusFX.images().load(label, AppImageConfig.System.MENU);
     */
    public void load(Label target, String iconKey) {
        if (target == null || iconKey == null || iconKey.trim().isEmpty()) {
            LOGGER.warn("Parâmetros inválidos para load(Label): target={}, key={}", target, iconKey);
            return;
        }

        try {
            Image image = loadImage(iconKey);
            if (image != null) {
                target.setGraphic(new ImageView(image));
                LOGGER.trace("Ícone aplicado ao Label: {}", iconKey);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao aplicar ícone no Label: {}", iconKey, e);
            target.setGraphic(null);
        }
    }

    /**
     * ✅ CARREGA IMAGEM PARA IMAGEVIEW COM TAMANHO ESPECÍFICO
     * Uso: NexusFX.images().load(imageView, "logo", 100, 100);
     */
    public void load(ImageView target, String imageKey, double width, double height) {
        if (target == null || imageKey == null || imageKey.trim().isEmpty()) {
            LOGGER.warn("Parâmetros inválidos para load(ImageView) com tamanho");
            return;
        }

        try {
            Image image = loadImage(imageKey);
            if (image != null) {
                target.setImage(image);
                target.setFitWidth(width);
                target.setFitHeight(height);
                target.setPreserveRatio(true);
                LOGGER.trace("Imagem com tamanho aplicada: {} ({}x{})", imageKey, width, height);
            }
        } catch (Exception e) {
            LOGGER.error("Erro ao aplicar imagem com tamanho: {}", imageKey, e);
        }
    }

    // ===== CARREGAMENTO DE IMAGEM =====

    /**
     * ✅ Carrega uma imagem pelo ID ou caminho
     */
    public Image loadImage(String pathOrKey) {
        try {
            Source source = detectSource(pathOrKey);
            return loadImage(pathOrKey, source);
        } catch (IOException e) {
            LOGGER.warn("Failed to load image: {}", pathOrKey, e);
            return null;
        }
    }

    /**
     * Carrega imagem com source específico
     */
    public Image loadImage(String pathOrKey, Source source) throws IOException {
        String cacheKey = buildCacheKey(pathOrKey, source);

        return Optional.ofNullable(imageCache.get(cacheKey))
                .map(SoftReference::get)
                .orElseGet(() -> {
                    try {
                        Image image = loadNewImage(pathOrKey, source);
                        addToCache(cacheKey, image);
                        return image;
                    } catch (IOException e) {
                        LOGGER.warn("Failed to load image: {}", pathOrKey, e);
                        throw new RuntimeException(e);
                    }
                });
    }

    // ===== CACHE =====

    private synchronized void addToCache(String cacheKey, Image image) {
        if (imageCache.size() >= maxCacheSize) {
            cleanUpCache();
        }
        imageCache.put(cacheKey, new SoftReference<>(image));
    }

    private synchronized void cleanUpCache() {
        Iterator<Map.Entry<String, SoftReference<Image>>> iterator = imageCache.entrySet().iterator();
        while (iterator.hasNext() && imageCache.size() >= maxCacheSize) {
            Map.Entry<String, SoftReference<Image>> entry = iterator.next();
            if (entry.getValue().get() == null) {
                iterator.remove();
            }
        }
        if (imageCache.size() >= maxCacheSize) {
            LOGGER.warn("Cache ainda muito grande após limpeza. Removendo entradas antigas...");
            // Remove 25% das entradas mais antigas
            int toRemove = maxCacheSize / 4;
            iterator = imageCache.entrySet().iterator();
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }
    }

    // ===== CARREGAMENTO INTERNO =====

    private Image loadNewImage(String pathOrKey, Source source) throws IOException {
        try (InputStream stream = getImageStream(pathOrKey, source)) {
            if (stream == null) {
                throw new FileNotFoundException("Image resource not found: " + pathOrKey);
            }
            return new Image(stream);
        }
    }

    private InputStream getImageStream(String pathOrKey, Source source) throws IOException {
        switch (source) {
            case CONFIG:
                return imageRegistry
                        .getImagePath(pathOrKey)
                        .map(p -> getClass().getResourceAsStream(p))
                        .orElseThrow(() -> new FileNotFoundException("Image not registered: " + pathOrKey));

            case FILE:
                return new FileInputStream(pathOrKey);

            case URL:
                return new URL(pathOrKey).openStream();

            case RESOURCE:
                return getClass().getResourceAsStream(pathOrKey);

            default:
                throw new IllegalArgumentException("Unsupported image source");
        }
    }

    private Source detectSource(String pathOrKey) {
        if (pathOrKey == null || pathOrKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Image path/key cannot be null or empty");
        }

        if (pathOrKey.matches("^(https?|ftp)://.*")) {
            return Source.URL;
        }

        if (imageRegistry.isRegistered(pathOrKey)) {
            return Source.CONFIG;
        }

        if (new File(pathOrKey).isAbsolute()) {
            return Source.FILE;
        }

        return Source.RESOURCE;
    }

    private String buildCacheKey(String pathOrKey, Source source) {
        return source.name() + "::" + pathOrKey;
    }

    // ===== MÉTODOS DE CONVENIÊNCIA =====

    public ImageView addImageToContainer(String pathOrKey, Pane container,
                                         double width, double height) {
        try {
            Image image = loadImage(pathOrKey);
            if (image != null) {
                ImageView imageView = createImageView(image, width, height);
                container.getChildren().add(imageView);
                return imageView;
            }
        } catch (Exception e) {
            LOGGER.warn("Using placeholder for failed image: {}", pathOrKey, e);
        }
        ImageView placeholder = createPlaceholderView(width, height);
        container.getChildren().add(placeholder);
        return placeholder;
    }

    public ImageView createImageView(Image image, double width, double height) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    private ImageView createPlaceholderView(double width, double height) {
        ImageView placeholder = new ImageView();
        placeholder.setFitWidth(width);
        placeholder.setFitHeight(height);
        placeholder.setStyle("-fx-background-color: #eeeeee;");
        return placeholder;
    }

    // ===== GERENCIAMENTO DE CACHE =====

    public void preloadImages(String... imagePaths) {
        Arrays.stream(imagePaths)
                .parallel()
                .forEach(path -> {
                    try {
                        loadImage(path);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to preload image: {}", path, e);
                    }
                });
    }

    public void removeImageFromCache(String pathOrKey) {
        Source source = detectSource(pathOrKey);
        String cacheKey = buildCacheKey(pathOrKey, source);
        imageCache.remove(cacheKey);
        LOGGER.info("Image removed from cache: {}", cacheKey);
    }

    public void clearImageCache() {
        int size = imageCache.size();
        imageCache.clear();
        LOGGER.info("Image cache cleared ({} entries)", size);
    }

    public boolean isImageCached(String pathOrKey) {
        Source source = detectSource(pathOrKey);
        String cacheKey = buildCacheKey(pathOrKey, source);
        return imageCache.containsKey(cacheKey)
                && imageCache.get(cacheKey).get() != null;
    }

    public int getCacheSize() {
        return imageCache.size();
    }

    public void setMaxCacheSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        this.maxCacheSize = size;
        LOGGER.info("Image cache size set to: {}", size);
    }
}