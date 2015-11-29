package io.katharsis.resource.registry;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.resource.registry.repository.WithRelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import org.reflections.Reflections;

import java.util.List;

/**
 * Using class of this type it's possible to build instances of repository entries, which can be used by other parts of
 * the library.
 */
public interface RepositoryEntryBuilder {

    ResourceEntry<?, ?> buildResourceRepository(Reflections reflections, Class<?> resourceClass);

    List<WithRelationshipEntry<RelationshipRepository, ?, ?>> buildRelationshipRepositories(Reflections reflections, Class<?> resourceClass);
}
