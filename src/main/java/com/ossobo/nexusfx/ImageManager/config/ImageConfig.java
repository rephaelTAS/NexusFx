package com.ossobo.nexusfx.ImageManager.config;

import java.net.URL;
import java.util.Objects;

/**
 * Representa a configuração de uma imagem, associando um nome lógico ao seu caminho (URL).
 * Esta classe é interna ao framework e não deve ser exposta publicamente.
 */
public final class  ImageConfig {

    private final String name;
    private final URL path;

    public ImageConfig(String name, URL path) {
        this.name = Objects.requireNonNull(name, "O nome do ImageConfig não pode ser nulo.");
        this.path = Objects.requireNonNull(path, "O caminho (URL) do ImageConfig não pode ser nulo.");
    }

    public String getName() {
        return name;
    }

    public URL getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageConfig that = (ImageConfig) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ImageConfig{" +
                "name='" + name + '\'' +
                ", path=" + path +
                '}';
    }
}
