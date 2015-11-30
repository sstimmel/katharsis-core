package io.katharsis.repository;

import java.io.Serializable;

/**
 * Marker interface which define that a repository implementation operates on a resource relationships.
 */
public interface WithRelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable> {
    int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;
}
