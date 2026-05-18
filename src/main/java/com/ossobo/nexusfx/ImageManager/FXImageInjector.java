package com.ossobo.nexusfx.ImageManager;

import com.ossobo.nexusfx.NexusFX;
import com.ossobo.nexusfx.di.annotations.FXImage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 🖼️ FXImageInjector - Processa a anotação @FXImage
 *
 * Injeta automaticamente imagens do NexusFX nos campos ImageView anotados.
 * Chamado automaticamente pelo FXMLService após carregar cada view FXML.
 *
 * ✅ v1.1: Usa ImageService (API pública) em vez de ImageRegistry interno
 * ✅ v1.1: Suporte a redimensionamento via parâmetros da anotação
 *
 * Uso no Controller:
 *   @FXML
 *   @FXImage(AppImageConfig.System.DASHBOARD)
 *   private ImageView dashboardIcon;
 *
 * @author Rafael Tavares
 * @since 1.1
 */
public final class FXImageInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FXImageInjector.class);

    private FXImageInjector() {
        throw new UnsupportedOperationException("Classe utilitária - não instanciar");
    }

    /**
     * Processa todas as anotações @FXImage em um controller.
     *
     * Chamado automaticamente pelo FXMLService após o FXMLLoader.load().
     *
     * @param controller Instância do controller FXML (já com @FXML populados)
     */
    public static void injectImages(Object controller) {
        if (controller == null) {
            LOGGER.warn("⚠️ Controller nulo, ignorando injeção de imagens");
            return;
        }

        Class<?> clazz = controller.getClass();
        LOGGER.debug("🔍 Processando @FXImage em: {}", clazz.getSimpleName());

        int injectedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (Field field : clazz.getDeclaredFields()) {
            FXImage fxImage = field.getAnnotation(FXImage.class);

            if (fxImage == null) {
                continue; // Não tem @FXImage, pula
            }

            // Verifica se o campo é do tipo ImageView
            if (!ImageView.class.isAssignableFrom(field.getType())) {
                LOGGER.warn("⚠️ @FXImage em campo não-ImageView: {}.{} (tipo: {})",
                        clazz.getSimpleName(), field.getName(), field.getType().getSimpleName());
                skippedCount++;
                continue;
            }

            try {
                field.setAccessible(true);
                ImageView imageView = (ImageView) field.get(controller);

                if (imageView == null) {
                    LOGGER.warn("⚠️ ImageView nulo para @FXImage: {}.{}",
                            clazz.getSimpleName(), field.getName());
                    skippedCount++;
                    continue;
                }

                // Tenta injetar a imagem
                boolean success = injectImage(imageView, fxImage);
                if (success) {
                    injectedCount++;
                } else {
                    errorCount++;
                }

            } catch (IllegalAccessException e) {
                LOGGER.error("❌ Erro ao acessar campo: {}.{}",
                        clazz.getSimpleName(), field.getName(), e);
                errorCount++;
            } catch (Exception e) {
                LOGGER.error("❌ Erro inesperado ao injetar imagem: {}.{}",
                        clazz.getSimpleName(), field.getName(), e);
                errorCount++;
            }
        }

        // Log resumido
        if (injectedCount > 0 || errorCount > 0) {
            LOGGER.info("🎨 @FXImage processado em {}: {} injetadas, {} ignoradas, {} erros",
                    clazz.getSimpleName(), injectedCount, skippedCount, errorCount);
        }
    }

    /**
     * Injeta uma imagem em um ImageView baseado na anotação @FXImage.
     *
     * @param imageView ImageView de destino
     * @param fxImage   Anotação @FXImage com os parâmetros
     * @return true se a imagem foi injetada com sucesso
     */
    private static boolean injectImage(ImageView imageView, FXImage fxImage) {
        String imageId = fxImage.value();

        if (imageId == null || imageId.isEmpty()) {
            LOGGER.warn("⚠️ @FXImage com ID vazio");
            return false;
        }

        try {
            // ✅ Usa ImageService (API pública) para carregar a imagem
            ImageService imageService = NexusFX.images();

            if (imageService == null) {
                LOGGER.warn("⚠️ ImageService não disponível");
                return false;
            }

            // Carrega a imagem pelo ID
            Image image = imageService.loadImage(imageId);

            if (image == null) {
                LOGGER.warn("⚠️ Imagem não encontrada: '{}'", imageId);
                return false;
            }

            // Aplica redimensionamento se especificado na anotação
            double width = fxImage.width();
            double height = fxImage.height();

            if (width > 0 && height > 0) {
                // Redimensiona carregando nova imagem com dimensões
                imageView.setImage(new Image(
                        image.getUrl(),
                        width,
                        height,
                        fxImage.preserveRatio(),
                        fxImage.smooth()
                ));
            } else if (width > 0) {
                // Apenas largura
                imageView.setFitWidth(width);
                imageView.setPreserveRatio(fxImage.preserveRatio());
                imageView.setImage(image);
            } else if (height > 0) {
                // Apenas altura
                imageView.setFitHeight(height);
                imageView.setPreserveRatio(fxImage.preserveRatio());
                imageView.setImage(image);
            } else {
                // Sem redimensionamento
                imageView.setImage(image);
            }

            LOGGER.debug("✅ Imagem '{}' injetada com sucesso", imageId);
            return true;

        } catch (Exception e) {
            LOGGER.error("❌ Erro ao injetar imagem '{}': {}", imageId, e.getMessage());
            return false;
        }
    }

    /**
     * Versão simplificada: injeta uma única imagem por ID em um ImageView.
     * Útil para injeção programática fora do ciclo FXML.
     *
     * @param imageView ImageView de destino
     * @param imageId   ID da imagem registrada no ResourceAPI
     * @return true se a imagem foi injetada com sucesso
     */
    public static boolean injectImage(ImageView imageView, String imageId) {
        if (imageView == null || imageId == null || imageId.isEmpty()) {
            return false;
        }

        try {
            ImageService imageService = NexusFX.images();
            if (imageService != null) {
                Image image = imageService.loadImage(imageId);
                if (image != null) {
                    imageView.setImage(image);
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao injetar imagem '{}': {}", imageId, e.getMessage());
        }
        return false;
    }

    /**
     * Injeta uma imagem com dimensões específicas.
     *
     * @param imageView ImageView de destino
     * @param imageId   ID da imagem
     * @param width     Largura desejada (0 = manter original)
     * @param height    Altura desejada (0 = manter original)
     * @return true se a imagem foi injetada com sucesso
     */
    public static boolean injectImage(ImageView imageView, String imageId,
                                      double width, double height) {
        if (imageView == null || imageId == null || imageId.isEmpty()) {
            return false;
        }

        try {
            ImageService imageService = NexusFX.images();
            if (imageService != null) {
                Image image = imageService.loadImage(imageId);
                if (image != null) {
                    if (width > 0 && height > 0) {
                        imageView.setImage(new Image(image.getUrl(), width, height, true, true));
                    } else {
                        imageView.setImage(image);
                        if (width > 0) imageView.setFitWidth(width);
                        if (height > 0) imageView.setFitHeight(height);
                        imageView.setPreserveRatio(true);
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao injetar imagem '{}': {}", imageId, e.getMessage());
        }
        return false;
    }
}