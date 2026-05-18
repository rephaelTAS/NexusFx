package com.ossobo.nexusfx.resources.descriptor;

import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ViewDescriptor v2.0
 *
 * Descritor unificado para Views e Alertas.
 * Uma view pode ser uma tela normal (VIEW) ou um alerta (ALERT).
 *
 * Modo VIEW:  usa controllerClass, primaryCss, additionalCss, cssMode
 * Modo ALERT: adicionalmente usa alertType, modality, soundUrl, iconUrl,
 *             confirmationRequired, autoCloseMillis
 *
 * v2.0 (24/04/2026):
 * - ✅ Unificado: ViewDescriptor substitui AlertDescriptor
 * - ✅ ModeUse define se é VIEW ou ALERT
 * - ✅ Campos de alerta são null/0 quando modeUse = VIEW
 * - ✅ Um único ponto de tratamento de FXML pelo ViewManager
 */
public final class ViewDescriptor extends ResourceDescriptor {

    // ===== ENUMS =====

    public enum ViewType { STATIC, DYNAMIC }
    public enum CssMode  { REPLACE, APPEND, NONE }
    public enum ModeUse  { VIEW, ALERT }
    public enum AlertType { INFO, WARNING, ERROR, CONFIRMATION, SUCCESS }
    public enum Modality  { APPLICATION_MODAL, WINDOW_MODAL, NONE }

    // ===== CAMPOS COMUNS (VIEW E ALERT) =====

    private final ViewType viewType;
    private final Class<?> controllerClass;
    private final CssMode cssMode;
    private final URL primaryCss;
    private final List<URL> additionalCss;
    private final ModeUse modeUse;

    // ===== CAMPOS EXCLUSIVOS DE ALERTA (null/0 quando VIEW) =====

    private final AlertType alertType;
    private final Modality modality;
    private final URL soundUrl;
    private final URL iconUrl;
    private final boolean confirmationRequired;
    private final long autoCloseMillis;

    // ===== CONSTRUTOR PRIVADO → USA BUILDER OU FÁBRICAS =====

    private ViewDescriptor(Builder builder) {
        super(builder.id, builder.fxmlUrl,
                builder.modeUse == ModeUse.ALERT ? ResourceType.ALERT : ResourceType.FXML,
                builder.origin);

        this.viewType = Objects.requireNonNullElse(builder.viewType, ViewType.STATIC);
        this.controllerClass = builder.controllerClass;
        this.cssMode = Objects.requireNonNullElse(builder.cssMode, CssMode.NONE);
        this.primaryCss = builder.primaryCss;
        this.additionalCss = builder.additionalCss != null
                ? List.copyOf(builder.additionalCss)
                : Collections.emptyList();
        this.modeUse = Objects.requireNonNull(builder.modeUse, "modeUse é obrigatório");

        // Campos de alerta — null/0 quando VIEW
        this.alertType = builder.alertType;
        this.modality = builder.modality;
        this.soundUrl = builder.soundUrl;
        this.iconUrl = builder.iconUrl;
        this.confirmationRequired = builder.confirmationRequired;
        this.autoCloseMillis = builder.autoCloseMillis;
    }

    // ===== GETTERS COMUNS =====

    public ViewType getViewType()           { return viewType; }
    public Class<?> getControllerClass()    { return controllerClass; }
    public CssMode getCssMode()             { return cssMode; }
    public URL getPrimaryCss()              { return primaryCss; }
    public List<URL> getAdditionalCss()     { return additionalCss; }
    public ModeUse getModeUse()             { return modeUse; }
    public URL getFxmlUrl()                 { return getUrl(); }

    // ===== GETTERS DE ALERTA (retornam null/0 se VIEW) =====

    public AlertType getAlertType()         { return alertType; }
    public Modality getModality()           { return modality; }
    public URL getSoundUrl()                { return soundUrl; }
    public URL getIconUrl()                 { return iconUrl; }
    public boolean isConfirmationRequired() { return confirmationRequired; }
    public long getAutoCloseMillis()        { return autoCloseMillis; }

    // ===== CONVENIÊNCIA =====

    public boolean isAlert() { return modeUse == ModeUse.ALERT; }
    public boolean isView()  { return modeUse == ModeUse.VIEW; }

    // ===== BUILDER =====

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private URL fxmlUrl;
        private ResourceOrigin origin = ResourceOrigin.FRAMEWORK;

        // Comuns
        private ViewType viewType = ViewType.STATIC;
        private Class<?> controllerClass;
        private CssMode cssMode = CssMode.NONE;
        private URL primaryCss;
        private List<URL> additionalCss;
        private ModeUse modeUse = ModeUse.VIEW;

        // Alerta
        private AlertType alertType;
        private Modality modality;
        private URL soundUrl;
        private URL iconUrl;
        private boolean confirmationRequired;
        private long autoCloseMillis;

        public Builder id(String id)                     { this.id = id; return this; }
        public Builder fxmlUrl(URL url)                  { this.fxmlUrl = url; return this; }
        public Builder origin(ResourceOrigin origin)     { this.origin = origin; return this; }
        public Builder viewType(ViewType vt)             { this.viewType = vt; return this; }
        public Builder controllerClass(Class<?> cc)      { this.controllerClass = cc; return this; }
        public Builder cssMode(CssMode cm)               { this.cssMode = cm; return this; }
        public Builder primaryCss(URL css)               { this.primaryCss = css; return this; }
        public Builder additionalCss(List<URL> css)      { this.additionalCss = css; return this; }
        public Builder modeUse(ModeUse mu)               { this.modeUse = mu; return this; }

        // Alerta
        public Builder alertType(AlertType at)           { this.alertType = at; return this; }
        public Builder modality(Modality m)              { this.modality = m; return this; }
        public Builder soundUrl(URL url)                 { this.soundUrl = url; return this; }
        public Builder iconUrl(URL url)                  { this.iconUrl = url; return this; }
        public Builder confirmationRequired(boolean cr)  { this.confirmationRequired = cr; return this; }
        public Builder autoCloseMillis(long ms)          { this.autoCloseMillis = ms; return this; }

        /** Atalho: configura como VIEW */
        public Builder asView() {
            this.modeUse = ModeUse.VIEW;
            return this;
        }

        /** Atalho: configura como ALERT com AlertType */
        public Builder asAlert(AlertType type) {
            this.modeUse = ModeUse.ALERT;
            this.alertType = type;
            return this;
        }

        public ViewDescriptor build() {
            Objects.requireNonNull(id, "id é obrigatório");
            Objects.requireNonNull(fxmlUrl, "fxmlUrl é obrigatório");
            Objects.requireNonNull(modeUse, "modeUse é obrigatório");

            if (modeUse == ModeUse.ALERT) {
                Objects.requireNonNull(alertType, "alertType é obrigatório para ALERT");
                Objects.requireNonNull(modality, "modality é obrigatório para ALERT");
            }

            return new ViewDescriptor(this);
        }
    }
}