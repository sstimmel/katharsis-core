package io.katharsis.repository.mock;

import io.katharsis.repository.RepositoryMethodParameterProvider;

import java.lang.reflect.Method;

public class NewInstanceRepositoryMethodParameterProvider implements RepositoryMethodParameterProvider {


    @Override
    public <T> T provide(Method method, int parameterIndex) {
        try {
            return (T) method.getParameterTypes()[parameterIndex].newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}