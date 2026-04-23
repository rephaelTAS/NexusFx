package com.ossobo.nexusfx.resources.descriptor;

import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ViewDescriptor v1.0
 * Descreve uma view FXML e seus recursos CSS associados.
 */
public final class ViewDescriptor extends ResourceDescriptor {
    private final Class<?> controllerClass;
    private final URL primaryCss;
    private final List<URL> additionalCss;
    private final CssMode cssMode;
    private final ViewType viewType;

    public enum ViewType { STATIC, DYNAMIC }
    public enum CssMode { REPLACE, APPEND, NONE }

    public ViewDescriptor(String viewId, URL fxmlUrl, Class<?> controllerClass,
                          URL primaryCss, List<URL> additionalCss,
                          CssMode cssMode, ViewType viewType, ResourceOrigin origin) {
        super(viewId, fxmlUrl, ResourceType.FXML, origin);
        this.controllerClass = controllerClass;
        this.primaryCss = primaryCss;
        this.additionalCss = additionalCss != null ?
                List.copyOf(additionalCss) : Collections.emptyList();
        this.cssMode = Objects.requireNonNullElse(cssMode, CssMode.NONE);
        this.viewType = Objects.requireNonNullElse(viewType, ViewType.STATIC);
    }

    public Class<?> getControllerClass() { return controllerClass; }
    public URL getPrimaryCss() { return primaryCss; }
    public List<URL> getAdditionalCss() { return additionalCss; }
    public CssMode getCssMode() { return cssMode; }
    public ViewType getViewType() { return viewType; }
    public URL getFxmlUrl() { return getUrl(); }
}
