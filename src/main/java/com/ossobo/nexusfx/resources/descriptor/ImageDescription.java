package com.ossobo.nexusfx.resources.descriptor;

import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import java.net.URL;

/**
 * ImageDescription v1.0
 * Descreve uma imagem ou ícone com metadados visuais.
 */
public final class ImageDescription extends ResourceDescriptor {
    private final ImageType imageType;
    private final double preferredWidth;
    private final double preferredHeight;
    private final boolean preserveRatio;
    private final boolean smooth;
    private final String description;

    public enum ImageType { IMAGE, ICON, BACKGROUND }

    public ImageDescription(String id, URL url, ImageType imageType,
                            double preferredWidth, double preferredHeight,
                            boolean preserveRatio, boolean smooth,
                            String description, ResourceOrigin origin) {
        super(id, url, ResourceType.IMAGE, origin);
        this.imageType = imageType;
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.preserveRatio = preserveRatio;
        this.smooth = smooth;
        this.description = description;
    }

    public ImageType getImageType() { return imageType; }
    public double getPreferredWidth() { return preferredWidth; }
    public double getPreferredHeight() { return preferredHeight; }
    public boolean isPreserveRatio() { return preserveRatio; }
    public boolean isSmooth() { return smooth; }
    public String getDescription() { return description; }
}
