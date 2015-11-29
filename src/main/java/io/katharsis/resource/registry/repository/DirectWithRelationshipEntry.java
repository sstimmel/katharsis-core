package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RelationshipRepository;
import net.jodah.typetools.TypeResolver;

public class DirectWithRelationshipEntry<R_TYPE, T, D> implements WithRelationshipEntry<R_TYPE, T, D> {

    private R_TYPE repository;

    public DirectWithRelationshipEntry(R_TYPE repository) {
        this.repository = repository;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(RelationshipRepository.class, repository.getClass());
        return typeArgs[RelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public R_TYPE getRepository() {
        return repository;
    }
}
