package io.katharsis.resource.registry;

import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.repository.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds information about a resource of type <i>T</i> and its repositories.
 * It includes the following information:
 * - ResourceInformation instance with information about the resource,
 * - ResourceEntry instance,
 * - List of all repositories for relationships defined in resource class.
 * - Parent RegistryEntry if a resource inherits from another resource
 *
 * @param <T> resource type
 */
public class RegistryEntry<T> {
    private final ResourceInformation resourceInformation;
    private final ResourceEntry<T, ?> resourceEntry;
    private final List<WithRelationshipEntry<RelationshipRepository, T, ?>> relationshipRepoEntries;
    private final List<WithRelationshipEntry<FieldRepository, T, ?>> fieldRepoEntries;
    private RegistryEntry parentRegistryEntry = null;

    public RegistryEntry(ResourceInformation resourceInformation,
                         @SuppressWarnings("SameParameterValue") ResourceEntry<T, ?> resourceEntry) {
        this(resourceInformation, resourceEntry, new LinkedList<>(), new LinkedList<>());
    }

    public RegistryEntry(ResourceInformation resourceInformation,
                         ResourceEntry<T, ?> resourceEntry,
                         List<WithRelationshipEntry<RelationshipRepository, T, ?>> relationshipRepoEntries,
                         List<WithRelationshipEntry<FieldRepository, T, ?>> fieldRepoEntries) {
        this.resourceInformation = resourceInformation;
        this.resourceEntry = resourceEntry;
        this.relationshipRepoEntries = relationshipRepoEntries;
        this.fieldRepoEntries = fieldRepoEntries;
    }

    public ResourceRepository<T, ?> getResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
        ResourceRepository<T, ?> repoInstance = null;
        if (resourceEntry instanceof DirectResourceEntry) {
            repoInstance = ((DirectResourceEntry<T, ?>) resourceEntry).getResourceRepository();
        } else if (resourceEntry instanceof AnnotatedResourceEntryBuilder) {
            repoInstance = ((AnnotatedResourceEntryBuilder<T, ?>) resourceEntry).build(parameterProvider);
        }
        return repoInstance;
    }

    public List<WithRelationshipEntry<RelationshipRepository, T, ?>> getRelationshipRepoEntries() {
        return relationshipRepoEntries;
    }

    public RelationshipRepository<T, ?, ?, ?> getRelationshipRepositoryForClass(Class clazz, RepositoryMethodParameterProvider parameterProvider) {
        WithRelationshipEntry<RelationshipRepository, T, ?> foundWithRelationshipEntry = null;
        for (WithRelationshipEntry<RelationshipRepository, T, ?> withRelationshipEntry : relationshipRepoEntries) {
            if (clazz == withRelationshipEntry.getTargetAffiliation()) {
                foundWithRelationshipEntry = withRelationshipEntry;
                break;
            }
        }
        if (foundWithRelationshipEntry == null) {
            throw new RelationshipRepositoryNotFoundException(resourceInformation.getResourceClass(), clazz);
        }

        RelationshipRepository<T, ?, ?, ?> repoInstance = null;
        if (foundWithRelationshipEntry instanceof DirectWithRelationshipEntry) {
            repoInstance = ((DirectWithRelationshipEntry<RelationshipRepository, T, ?>) foundWithRelationshipEntry).getRepository();
        } else if (foundWithRelationshipEntry instanceof AnnotatedWithRelationshipEntryBuilder) {
            repoInstance = ((AnnotatedWithRelationshipEntryBuilder<RelationshipRepository, T, ?>) foundWithRelationshipEntry).build(parameterProvider);
        }

        return repoInstance;
    }

    public List<WithRelationshipEntry<FieldRepository, T, ?>> getFieldRepoEntries() {
        return fieldRepoEntries;
    }

    public FieldRepository<T, ?, ?, ?> getFieldRepositoryForClass(Class clazz, RepositoryMethodParameterProvider parameterProvider) {
        WithRelationshipEntry<FieldRepository, T, ?> foundWithRelationshipEntry = null;
        for (WithRelationshipEntry<FieldRepository, T, ?> withRelationshipEntry : fieldRepoEntries) {
            if (clazz == withRelationshipEntry.getTargetAffiliation()) {
                foundWithRelationshipEntry = withRelationshipEntry;
                break;
            }
        }
        if (foundWithRelationshipEntry == null) {
            throw new RelationshipRepositoryNotFoundException(resourceInformation.getResourceClass(), clazz);
        }

        FieldRepository<T, ?, ?, ?> repoInstance = null;
        if (foundWithRelationshipEntry instanceof DirectWithRelationshipEntry) {
            repoInstance = ((DirectWithRelationshipEntry<FieldRepository, T, ?>) foundWithRelationshipEntry).getRepository();
        } else if (foundWithRelationshipEntry instanceof AnnotatedWithRelationshipEntryBuilder) {
            repoInstance = ((AnnotatedWithRelationshipEntryBuilder<FieldRepository, T, ?>) foundWithRelationshipEntry).build(parameterProvider);
        }

        return repoInstance;
    }

    public ResourceInformation getResourceInformation() {
        return resourceInformation;
    }

    public RegistryEntry getParentRegistryEntry() {
        return parentRegistryEntry;
    }

    /**
     * To be used only by ResourceRegistryBuilder
     *
     * @param parentRegistryEntry parent resource
     */
    void setParentRegistryEntry(RegistryEntry parentRegistryEntry) {
        this.parentRegistryEntry = parentRegistryEntry;
    }

    /**
     * Check the parameter is a parent of <b>this</b> {@link RegistryEntry} instance
     *
     * @param registryEntry parent to check
     * @return true if the parameter is a parent
     */
    public boolean isParent(RegistryEntry registryEntry) {
        RegistryEntry parentRegistryEntry = getParentRegistryEntry();
        while (parentRegistryEntry != null) {
            if (parentRegistryEntry.equals(registryEntry)) {
                return true;
            }
            parentRegistryEntry = parentRegistryEntry.getParentRegistryEntry();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryEntry<?> that = (RegistryEntry<?>) o;
        return Objects.equals(resourceInformation, that.resourceInformation) &&
            Objects.equals(resourceEntry, that.resourceEntry) &&
            Objects.equals(relationshipRepoEntries, that.relationshipRepoEntries) &&
            Objects.equals(parentRegistryEntry, that.parentRegistryEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceInformation, resourceEntry, relationshipRepoEntries, parentRegistryEntry);
    }
}
