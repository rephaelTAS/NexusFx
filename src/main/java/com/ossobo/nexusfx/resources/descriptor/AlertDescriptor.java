package com.ossobo.nexusfx.resources.descriptor;

import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;

import java.net.URL;

/**
 * AlertDescriptor v1.0
 * Descreve um alerta modal com configurações completas.
 */
public final class AlertDescriptor extends ResourceDescriptor {
    private final AlertType alertType;
    private final Modality modality;
    private final URL soundUrl;
    private final URL iconUrl;
    private final boolean confirmationRequired;
    private final long autoCloseMillis;

    public enum AlertType { INFO, WARNING, ERROR, CONFIRMATION, SUCCESS }
    public enum Modality { APPLICATION_MODAL, WINDOW_MODAL, NONE }

    public AlertDescriptor(String id, URL fxmlUrl, AlertType alertType,
                           Modality modality, URL soundUrl, URL iconUrl,
                           boolean confirmationRequired, long autoCloseMillis,
                           ResourceOrigin origin) {
        super(id, fxmlUrl, ResourceType.ALERT, origin);
        this.alertType = alertType;
        this.modality = modality;
        this.soundUrl = soundUrl;
        this.iconUrl = iconUrl;
        this.confirmationRequired = confirmationRequired;
        this.autoCloseMillis = autoCloseMillis;
    }

    public AlertType getAlertType() { return alertType; }
    public Modality getModality() { return modality; }
    public URL getSoundUrl() { return soundUrl; }
    public URL getIconUrl() { return iconUrl; }
    public boolean isConfirmationRequired() { return confirmationRequired; }
    public long getAutoCloseMillis() { return autoCloseMillis; }
    public URL getFxmlUrl() { return getUrl(); }
}
