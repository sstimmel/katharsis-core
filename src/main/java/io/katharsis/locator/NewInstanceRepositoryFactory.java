package io.katharsis.locator;

/**
 * Sample implementation of {@link RepositoryFactory}. It makes new instance for every method call.
 */
public class NewInstanceRepositoryFactory implements RepositoryFactory {

    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <R> R build(Class<R> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
