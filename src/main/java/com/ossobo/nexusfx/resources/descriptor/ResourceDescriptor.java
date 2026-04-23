package com.ossobo.nexusfx.resources.descriptor;

import com.ossobo.nexusfx.resources.enums.ResourceOrigin;
import com.ossobo.nexusfx.resources.enums.ResourceType;
import java.net.URL;
import java.util.Objects;

/**
 * ResourceDescriptor v1.0
 * Representa o metadado base de qualquer recurso.
 * Imutável por design.
 */
public abstract class ResourceDescriptor {
    private final String id;
    private final URL url;
    private final ResourceType type;
    private final ResourceOrigin origin;

    protected ResourceDescriptor(String id, URL url, ResourceType type, ResourceOrigin origin) {
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo");
        this.url = Objects.requireNonNull(url, "URL não pode ser nula");
        this.type = Objects.requireNonNull(type, "Tipo não pode ser nulo");
        this.origin = Objects.requireNonNull(origin, "Origem não pode ser nula");
    }

    public String getId() { return id; }
    public URL getUrl() { return url; }
    public ResourceType getType() { return type; }
    public ResourceOrigin getOrigin() { return origin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceDescriptor that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
