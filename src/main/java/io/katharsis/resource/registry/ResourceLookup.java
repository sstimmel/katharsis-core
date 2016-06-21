package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;

import java.util.Map;
import java.util.Set;

public interface ResourceLookup {

    /**
     * Returns all resources keyed by resource name.
     *
     * @return
     */
    Map<String, Class<?>> getResources();

    /**
     * Returns a map of repositories keyed by resource name.
     *
     * @return
     */
    Map<String, Class<?>> getRepositories();

    @Deprecated
    Set<Class<?>> getResourceClasses();

    /**
     * Returns the repository classes {@link ResourceRepository}, {@link RelationshipRepository}.
     *
     * @return repository classes
     */
    @Deprecated
    Set<Class<?>> getResourceRepositoryClasses();

}
