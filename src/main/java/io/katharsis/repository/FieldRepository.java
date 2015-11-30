package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;

public interface FieldRepository<T, T_ID, D, D_ID> extends WithRelationshipRepository {

    int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

    D addField(T_ID resourceId, D field, String fieldName, QueryParams queryParams);

    Iterable<D> addFields(T_ID resourceId, Iterable<D> fields, String fieldName, QueryParams queryParams);

    void deleteField(T_ID resourceId, String fieldName, QueryParams queryParams);

    void deleteFields(T_ID resourceId, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams);
}
