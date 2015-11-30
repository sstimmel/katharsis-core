package io.katharsis.resource.registry.repository;

import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.WithRelationshipRepository;
import net.jodah.typetools.TypeResolver;

public class DirectWithRelationshipEntry<R_TYPE, T, D> implements WithRelationshipEntry<R_TYPE, T, D> {

    private final R_TYPE repository;
    private final Class<? extends WithRelationshipRepository> repoClazz;

    public DirectWithRelationshipEntry(R_TYPE repository, Class<? extends WithRelationshipRepository> repoClazz) {
        this.repository = repository;
        this.repoClazz = repoClazz;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?>[] typeArgs = TypeResolver
            .resolveRawArguments(repoClazz, repository.getClass());
        return typeArgs[WithRelationshipRepository.TARGET_TYPE_GENERIC_PARAMETER_IDX];
    }

    public R_TYPE getRepository() {
        return repository;
    }
}
