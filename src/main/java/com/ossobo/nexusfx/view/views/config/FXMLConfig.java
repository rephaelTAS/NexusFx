package com.ossobo.nexusfx.view.views.config;

import java.net.URL;
import java.util.Objects;

/**
 * Representa a configuração de um FXML - MANTIDO COMPATÍVEL
 */
final class FXMLConfig {
    private final String name;
    private final URL path;

    public FXMLConfig(String name, URL path) {
        this.name = Objects.requireNonNull(name, "Nome não pode ser nulo");
        this.path = Objects.requireNonNull(path, "Path não pode ser nulo");
    }

    public String getName() { return name; }
    public URL getPath() { return path; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FXMLConfig that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() {
        return "FXMLConfig{name='" + name + "', path=" + path + '}';
    }
}
