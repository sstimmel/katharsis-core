package io.katharsis.repository;

import io.katharsis.queryParams.QueryParams;

public interface FieldRepository<T, T_ID, D, D_ID> extends WithRelationshipRepository {

    int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

    D addField(T_ID resource, D field, String fieldName, QueryParams queryParams);

    Iterable<D> addFields(T_ID resource, Iterable<D> fields, String fieldName, QueryParams queryParams);

    void deleteField(T_ID resource, String fieldName, QueryParams queryParams);

    void deleteFields(T_ID resource, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams);
}
