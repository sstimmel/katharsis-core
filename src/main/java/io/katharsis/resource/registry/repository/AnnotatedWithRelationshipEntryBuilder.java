package io.katharsis.resource.registry.repository;

import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.adapter.FieldRepositoryAdapter;
import io.katharsis.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.repository.annotations.JsonApiFieldRepository;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;

import java.lang.annotation.Annotation;

public class AnnotatedWithRelationshipEntryBuilder<R_TYPE, T, D> implements WithRelationshipEntry<R_TYPE, T, D> {

    private final Object repositoryInstance;
    private final Class<? extends R_TYPE> repositoryClass;
    private final Class<? extends Annotation> repositoryAnnotationClass;

    public AnnotatedWithRelationshipEntryBuilder(Object repositoryInstance, Class<? extends R_TYPE> repositoryClass,
                                                 Class<? extends Annotation> repositoryAnnotationClass) {
        this.repositoryInstance = repositoryInstance;
        this.repositoryClass = repositoryClass;
        this.repositoryAnnotationClass = repositoryAnnotationClass;
    }

    @Override
    public Class<?> getTargetAffiliation() {
        Class<?> targetAffiliation;
        if (JsonApiRelationshipRepository.class.equals(repositoryAnnotationClass)) {
            targetAffiliation = repositoryInstance.getClass()
                .getAnnotation(JsonApiRelationshipRepository.class)
                .target();
        } else if (JsonApiFieldRepository.class.equals(repositoryAnnotationClass)) {
            targetAffiliation = repositoryInstance.getClass()
                .getAnnotation(JsonApiFieldRepository.class)
                .target();
        } else {
            throw new RuntimeException("Unsupported repository annotation: " + repositoryAnnotationClass);
        }
        return targetAffiliation;
    }

    public R_TYPE build(RepositoryMethodParameterProvider parameterProvider) {
        R_TYPE repository;
        if (RelationshipRepository.class.equals(repositoryClass)) {
            repository = (R_TYPE) new RelationshipRepositoryAdapter<>(repositoryInstance, new ParametersFactory(parameterProvider));
        } else if (FieldRepository.class.equals(repositoryClass)) {
            repository = (R_TYPE) new FieldRepositoryAdapter<>(repositoryInstance, new ParametersFactory(parameterProvider));
        } else {
            throw new RuntimeException("Unsupported repository type: " + repositoryClass);
        }

        return repository;
    }
}
