package io.katharsis.dispatcher.controller;

import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;

public class Utils {

    /**
     * TODO: ieugen: this might be better placed inside resourceRegistry.getEntry(resourceName);
     *
     * @param registryEntry
     * @param resourceName
     */
    public static void checkResourceExists(RegistryEntry registryEntry, String resourceName) {
        if (registryEntry == null) {
            throw new ResourceNotFoundException(resourceName);
        }
    }

}