package io.katharsis.repository;

import java.lang.reflect.Method;

/**
 * Provides additional parameters for a repository method.
 */
public interface RepositoryMethodParameterProvider {

    <T> T provide(Method method, int parameterIndex);

}
