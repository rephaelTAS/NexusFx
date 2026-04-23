package com.ossobo.nexusfx.resources.registry;

import com.ossobo.nexusfx.resources.descriptor.ResourceDescriptor;
import com.ossobo.nexusfx.resources.enums.ResourceType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * ResourceRegistry v1.0
 * Catálogo central - fonte única da verdade.
 */
public final class ResourceRegistry {
    private static final Logger LOGGER = Logger.getLogger(ResourceRegistry.class.getName());
    private final Map<String, ResourceDescriptor> descriptors = new ConcurrentHashMap<>();

    public void register(ResourceDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor não pode ser nulo");
        descriptors.put(descriptor.getId(), descriptor);
        LOGGER.fine(() -> "Registrado: " + descriptor.getId());
    }

    public Optional<ResourceDescriptor> findById(String id) {
        return Optional.ofNullable(descriptors.get(id));
    }

    public Optional<ResourceDescriptor> findByIdAndType(String id, ResourceType type) {
        return findById(id).filter(d -> d.getType() == type);
    }

    public boolean contains(String id) {
        return descriptors.containsKey(id);
    }

    public void unregister(String id) {
        descriptors.remove(id);
        LOGGER.fine(() -> "Removido: " + id);
    }

    public List<ResourceDescriptor> findAll() {
        return List.copyOf(descriptors.values());
    }

    public int count() {
        return descriptors.size();
    }

    public void clear() {
        descriptors.clear();
        LOGGER.info("Registry limpo");
    }
}
