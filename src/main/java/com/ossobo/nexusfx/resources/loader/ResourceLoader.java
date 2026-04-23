/*
 * ResourceLoader v1.0 (OPCIONAL)
 *
 * Responsabilidade: transformar recurso bruto em algo consumível por outro módulo.
 * Entrada: URL ou InputStream vindo do resolver.
 * Saída: Parent, Image, CSS aplicado, player, etc.
 * Depende de: ResourceResolver.
 *
 * NOTA: Este loader é opcional. Os módulos consumidores podem implementar
 * seus próprios loaders usando diretamente o ResourceResolver.
 */

package com.ossobo.nexusfx.resources.loader;

import com.ossobo.nexusfx.resources.excecoes.ResourceLoadException;
import com.ossobo.nexusfx.resources.resolver.ResourceResolver;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🔧 ResourceLoader v1.0 (OPCIONAL)
 * <p>
 * Conjunto de helpers para carregamento de recursos específicos.
 * Usa ResourceResolver para obter URLs.
 * </p>
 *
 * <pre>
 * Uso típico:
 *   Parent view = loader.loadFxml("login-view");
 *   Image logo = loader.loadImage("app-logo");
 *   AudioClip sound = loader.loadAudioClip("notification");
 * </pre>
 */
public final class ResourceLoader {

    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class.getName());

    private final ResourceResolver resolver;

    /**
     * Construtor que recebe o resolver como dependência.
     */
    public ResourceLoader(ResourceResolver resolver) {
        this.resolver = resolver;
        LOGGER.info("🔧 ResourceLoader v1.0 inicializado");
    }

    // ===== FXML =====

    /**
     * Carrega um arquivo FXML como Parent.
     *
     * @param viewId ID da view registrada
     * @return Parent (raiz da cena)
     * @throws ResourceLoadException Se falhar ao carregar
     */
    public Parent loadFxml(String viewId) {
        URL url = resolver.getViewUrl(viewId);

        try {
            FXMLLoader loader = new FXMLLoader(url);
            return loader.load();
        } catch (IOException e) {
            throw new ResourceLoadException(viewId, "FXML", e);
        }
    }

    /**
     * Carrega FXML com controller customizado.
     *
     * @param viewId ID da view registrada
     * @param controllerType Tipo esperado do controller
     * @return Resultado contendo root e controller
     */
    public <T> FxmlLoadResult<T> loadFxmlWithController(String viewId, Class<T> controllerType) {
        URL url = resolver.getViewUrl(viewId);

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controllerType.isInstance(controller)) {
                return new FxmlLoadResult<>(root, controllerType.cast(controller));
            } else {
                throw new ResourceLoadException(
                        viewId,
                        "FXML",
                        "Controller não é do tipo esperado: " + controllerType.getName()
                );
            }
        } catch (IOException e) {
            throw new ResourceLoadException(viewId, "FXML", e);
        }
    }

    /**
     * Resultado do carregamento de FXML com controller.
     */
    public record FxmlLoadResult<T>(Parent root, T controller) {}

    // ===== IMAGEM =====

    /**
     * Carrega uma imagem com configurações padrão.
     */
    public Image loadImage(String imageId) {
        URL url = resolver.getImageUrl(imageId);
        return new Image(url.toExternalForm());
    }

    /**
     * Carrega imagem com dimensões específicas.
     */
    public Image loadImage(String imageId, double width, double height,
                           boolean preserveRatio, boolean smooth) {
        URL url = resolver.getImageUrl(imageId);
        return new Image(url.toExternalForm(), width, height, preserveRatio, smooth);
    }

    /**
     * Carrega imagem em background com callback.
     */
    public void loadImageAsync(String imageId, ImageLoadCallback callback) {
        URL url = resolver.getImageUrl(imageId);
        Image image = new Image(url.toExternalForm(), true);

        image.progressProperty().addListener((obs, old, progress) -> {
            if (progress.doubleValue() >= 1.0 && !image.isError()) {
                Platform.runLater(() -> callback.onLoaded(image));
            }
        });

        image.errorProperty().addListener((obs, old, error) -> {
            if (error) {
                Platform.runLater(() -> callback.onError(
                        new ResourceLoadException(imageId, "IMAGE", "Falha ao carregar imagem")
                ));
            }
        });
    }

    @FunctionalInterface
    public interface ImageLoadCallback {
        void onLoaded(Image image);

        default void onError(ResourceLoadException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar imagem", e);
        }
    }

    // ===== CSS =====

    /**
     * Obtém URL do CSS como string externa (para Scene.getStylesheets().add()).
     */
    public String getCssExternalForm(String cssId) {
        return resolver.getCssUrl(cssId).toExternalForm();
    }

    /**
     * Obtém múltiplos CSS como lista de strings.
     */
    public java.util.List<String> getCssExternalForms(String... cssIds) {
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String cssId : cssIds) {
            result.add(getCssExternalForm(cssId));
        }
        return result;
    }

    // ===== SOM =====

    /**
     * Carrega AudioClip para sons curtos (WAV recomendado).
     * AudioClip carrega completamente na memória.
     */
    public AudioClip loadAudioClip(String soundId) {
        URL url = resolver.getSoundUrl(soundId);
        return new AudioClip(url.toExternalForm());
    }

    /**
     * Carrega AudioClip com configurações.
     */
    public AudioClip loadAudioClip(String soundId, double volume, int priority) {
        AudioClip clip = loadAudioClip(soundId);
        clip.setVolume(volume);
        clip.setPriority(priority);
        return clip;
    }

    /**
     * Carrega MediaPlayer para sons longos ou streaming (MP3).
     * MediaPlayer faz streaming, não carrega tudo na memória.
     */
    public MediaPlayer loadMediaPlayer(String soundId) {
        URL url = resolver.getSoundUrl(soundId);
        Media media = new Media(url.toExternalForm());
        return new MediaPlayer(media);
    }

    // ===== UTILITÁRIOS =====

    /**
     * Verifica se uma URL é acessível.
     */
    public boolean isAccessible(String resourceId) {
        try {
            resolver.resolveStream(resourceId).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
