package io.katharsis.dispatcher.registry.annotated;

import io.katharsis.query.QueryParams;
import io.katharsis.repository.annotations.JsonApiAddRelations;
import io.katharsis.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.repository.annotations.JsonApiFindOneTarget;
import io.katharsis.repository.annotations.JsonApiRemoveRelations;
import io.katharsis.repository.annotations.JsonApiSetRelation;
import io.katharsis.repository.annotations.JsonApiSetRelations;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

public class AnnotatedRelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
        extends AnnotatedRepositoryAdapter<T> {

    private Method setRelationMethod;
    private Method setRelationsMethod;
    private Method addRelationsMethod;
    private Method removeRelationsMethod;
    private Method findOneTargetMethod;
    private Method findManyTargetsMethod;

    public AnnotatedRelationshipRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
        setRelationMethod = ClassUtils.findMethodWith(implementationClass, JsonApiSetRelation.class);
        setRelationsMethod = ClassUtils.findMethodWith(implementationClass, JsonApiSetRelations.class);
        addRelationsMethod = ClassUtils.findMethodWith(implementationClass, JsonApiAddRelations.class);
        removeRelationsMethod = ClassUtils.findMethodWith(implementationClass, JsonApiRemoveRelations.class);
        findOneTargetMethod = ClassUtils.findMethodWith(implementationClass, JsonApiFindOneTarget.class);
        findManyTargetsMethod = ClassUtils.findMethodWith(implementationClass, JsonApiFindManyTargets.class);
    }

    public void setRelation(T source, D_ID targetId, String fieldName, QueryParams queryParams) {
        invokeOperation(setRelationMethod, new Object[]{source, targetId, fieldName}, queryParams);
    }

    public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        invokeOperation(setRelationsMethod, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        invokeOperation(addRelationsMethod, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        invokeOperation(removeRelationsMethod, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public Object findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
        return invokeOperation(findOneTargetMethod, new Object[]{sourceId, fieldName}, queryParams);
    }

    public Object findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
        return invokeOperation(findManyTargetsMethod, new Object[]{sourceId, fieldName}, queryParams);
    }
}
