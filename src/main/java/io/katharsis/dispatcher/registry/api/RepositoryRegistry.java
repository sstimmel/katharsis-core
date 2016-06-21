package io.katharsis.dispatcher.registry.api;

import io.katharsis.repository.exception.RepositoryNotFoundException;

/**
 * Returns an instance of a Repository for a given resource name.
 */
public interface RepositoryRegistry {

    Repository get(String resource) throws RepositoryNotFoundException;
}
