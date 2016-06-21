package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.registry.api.Repository;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.locator.RepositoryFactory;
import io.katharsis.repository.exception.RepositoryNotFoundException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
public class RepositoryRegistryImpl implements RepositoryRegistry {

    private Map<String, Class<?>> resources;
    private Map<String, Class<?>> repositories;

    private RepositoryFactory factory;

    private String packages;
    private String baseUrl;

    public RepositoryRegistryImpl(@NonNull String baseUrl,
                                  @NonNull Map<String, Class<?>> resources,
                                  @NonNull Map<String, Class<?>> repositories) {
        this.baseUrl = baseUrl;
        this.resources = resources;
        this.repositories = repositories;
    }

    public static RepositoryRegistryImpl build(String packages, String baseUrl) {
        DefaultResourceLookup resourceLookup = new DefaultResourceLookup(packages);

        return new RepositoryRegistryImpl(baseUrl, resourceLookup.getResources(), resourceLookup.getRepositories());
    }

    @Override
    public Repository get(String resource) throws RepositoryNotFoundException {
        Class<?> repozz = repositories.get(resource);
        if (repozz == null) {
            throw new RepositoryNotFoundException(resource);
        }
        return (Repository) factory.build(repozz);
    }
}
