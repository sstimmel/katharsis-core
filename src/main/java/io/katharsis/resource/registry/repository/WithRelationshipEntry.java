package io.katharsis.resource.registry.repository;

/**
 * Identifies a relationship repository entry
 */
public interface WithRelationshipEntry<R_TYPE, T, D> {

    /**
     * @return target class
     */
    Class<?> getTargetAffiliation();
}
