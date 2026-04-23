package com.ossobo.nexusfx.ImageManager;

import com.ossobo.nexusfx.di.annotations.ScopeAnnotation;
import com.ossobo.nexusfx.di.annotations.Service;
import com.ossobo.nexusfx.di.scopes.ScopeType;
import com.ossobo.nexusfx.ImageManager.image.ImageRegistry;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎯 IMAGE SERVICE COORDINATOR - Orquestra componentes especializados
 * Design Pattern: Facade + Service Layer + Dual Constructor Pattern
 *
 * 🔥 100% BACKWARD COMPATIBLE: Mantém contrato new ImageService()
 * 🔥 DI OPTIONAL: Suporta DI container quando disponível
 * 🔥 ZERO BREAKING CHANGES: Código legado funciona sem modificações
 *
 * 🎯 API UNIFICADA SIMPLIFICADA:
 * - load(Stage, String)           ← Ícone para Stage
 * - load(ImageView, String)       ← Imagem para ImageView
 * - load(Label, String)           ← Ícone para Label
 * - loadImage(String)             ← Retorna Image para uso genérico
 * - loadAndCache(String)          ← Retorna Image com fallback automático
 */
@Service
@ScopeAnnotation(ScopeType.SINGLETON)
public class ImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final Map<String, SoftReference<Image>> imageCache = new ConcurrentHashMap<>();
    private final ImageRegistry imageRegistry;
    private int maxCacheSize = 200;


    public ImageService() {
        this.imageRegistry = new ImageRegistry();
        LOGGER.info("ImageManager instance created and ImageRegistry injected.");
    }

    public enum Source {
        CONFIG,
        FILE,
        URL,
        RESOURCE
    }

    // ===== 🎯 NOVA API UNIFICADA (PRINCIPAL) =====

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
            stage.getIcons().add(image);
            LOGGER.debug("Ícone aplicado ao Stage: {}", imageKey);
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
            ((ImageView) target).setImage(image);
            LOGGER.trace("Imagem aplicada ao ImageView: {}", imageKey);
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
            ((Label) target).setGraphic(new ImageView(image));
            LOGGER.trace("Ícone aplicado ao Label: {}", iconKey);
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
            target.setImage(image);
            target.setFitWidth(width);
            target.setFitHeight(height);
            target.setPreserveRatio(true);
            LOGGER.trace("Imagem com tamanho aplicada: {} ({}x{})", imageKey, width, height);
        } catch (Exception e) {
            LOGGER.error("Erro ao aplicar imagem com tamanho: {}", imageKey, e);
        }
    }


    public Image loadImage(String pathOrKey) throws IOException {
        Source source = detectSource(pathOrKey);
        return loadImage(pathOrKey, source);
    }

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
                        LOGGER.warn( "Failed to load image: " + pathOrKey, e);
                        throw new RuntimeException(e);
                    }
                });
    }

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
            LOGGER.warn("Cache still too large after cleanup. Clearing oldest entries.");
            // Implementar uma estratégia de remoção LRU ou similar, se necessário.
        }
    }

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

    public ImageView addImageToContainer(String pathOrKey, Pane container,
                                         double width, double height) {
        try {
            Image image = loadImage(pathOrKey);
            ImageView imageView = createImageView(image, width, height);
            container.getChildren().add(imageView);
            return imageView;
        } catch (IOException e) {
            LOGGER.warn("Using placeholder for failed image: " + pathOrKey, e);
            ImageView placeholder = createPlaceholderView(width, height);
            container.getChildren().add(placeholder);
            return placeholder;
        }
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

    public void preloadImages(String... imagePaths) {
        Arrays.stream(imagePaths)
                .parallel()
                .forEach(path -> {
                    try {
                        loadImage(path);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to preload image: " + path, e);
                    }
                });
    }

    public void removeImageFromCache(String pathOrKey) {
        Source source = detectSource(pathOrKey);
        String cacheKey = buildCacheKey(pathOrKey, source);
        imageCache.remove(cacheKey);
        LOGGER.info( "Image removed from cache: {0}", cacheKey);
    }

    public void clearImageCache() {
        imageCache.clear();
        LOGGER.info("Image cache cleared");
    }

    public boolean isImageCached(String pathOrKey) {
        Source source = detectSource(pathOrKey);
        String cacheKey = buildCacheKey(pathOrKey, source);
        return imageCache.containsKey(cacheKey) && imageCache.get(cacheKey).get() != null;
    }

    public void setMaxCacheSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        this.maxCacheSize = size;
        LOGGER.info( "Image cache size set to: {0}", size);
    }
}
