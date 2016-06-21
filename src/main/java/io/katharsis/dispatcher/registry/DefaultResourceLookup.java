package io.katharsis.dispatcher.registry;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.registry.ResourceLookup;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Looks up and loads Json APi resource and repository classes.
 */
@Data
@Slf4j
public class DefaultResourceLookup implements ResourceLookup {

    private Map<String, Class<?>> resources;
    private Map<String, Class<?>> repositories;

    private Reflections reflections;

    public DefaultResourceLookup(@NonNull String packages) {
        this.reflections = new Reflections(packages.split(","));

        this.resources = processResourceClasses();
        this.repositories = processRepositoryClasses();
    }

    protected Map<String, Class<?>> processResourceClasses() {
        Map<String, Class<?>> resources = new HashMap<>();
        for (Class resource : findResourceClasses()) {
            JsonApiResource res = getAnnotation(resource, JsonApiResource.class);
            if (resources.containsKey(res.type())) {
                log.error("Duplicate resource found for {}: {} and {}", res.type(),
                        resource.getCanonicalName(), resources.get(res.type()).getCanonicalName());
                throw new KatharsisInitializationException("Duplicate resource found for " + res.type());
            } else {
                log.info("Found JSON-API resource\t '{}': {}", res.type(), resource.getCanonicalName());
                resources.put(res.type(), resource);
            }
        }
        return resources;
    }

    private Map<String, Class<?>> processRepositoryClasses() {
        Map<String, Class<?>> repositories = new HashMap<>();
        for (Class repository : findRepositoryClasses()) {
            JsonApiResourceRepository res = getAnnotation(repository, JsonApiResourceRepository.class);

            String resourceName = getResourceName(res);
            checkResourceIsKnown(resourceName);

            if (repositories.containsKey(resourceName)) {
                log.error("Duplicate resource found for {}: {} and {}", resourceName, res.value().getCanonicalName(),
                        repositories.get(resourceName).getCanonicalName());
                throw new KatharsisInitializationException("Duplicate resource found for " + resourceName);
            } else {
                log.info("Found JSON-API repository\t '{}': {}", resourceName, repository.getCanonicalName());
                repositories.put(resourceName, repository);
            }
        }

        if (!resources.keySet().containsAll(repositories.keySet())) {
            log.warn("There are resources without repositories.");
        }

        return repositories;
    }

    private void checkResourceIsKnown(String resourceName) {
        if (!resources.containsKey(resourceName)) {
            throw new KatharsisInitializationException("Resource is not known in this registry: " + resourceName);
        }
    }

    private String getResourceName(JsonApiResourceRepository res) {
        Class resourceClass = res.value();
        JsonApiResource annotation = getAnnotation(resourceClass, JsonApiResource.class);
        return annotation.type();
    }

    private <T extends Annotation> T getAnnotation(Class classWithAnnotation, Class annotationToLookFor) {
        Annotation annotation = classWithAnnotation.getAnnotation(annotationToLookFor);
        if (annotation == null) {
            throw new KatharsisInitializationException(String
                    .format("Required annotation %s is missing from %s", annotationToLookFor, classWithAnnotation));
        }
        return (T) annotation;
    }

    protected Set<Class<?>> findResourceClasses() {
        return reflections.getTypesAnnotatedWith(JsonApiResource.class);
    }

    protected Set<Class<?>> findRepositoryClasses() {
        Set<Class<?>> annotatedResourceRepositories = reflections.getTypesAnnotatedWith(JsonApiResourceRepository.class);
        Set<Class<?>> result = new HashSet<>();
        result.addAll(annotatedResourceRepositories);

        return result;
    }

    @Override
    public Set<Class<?>> getResourceClasses() {
        Set<Class<?>> res = new HashSet<>();
        res.addAll(resources.values());
        return res;
    }

    @Override
    public Set<Class<?>> getResourceRepositoryClasses() {
        Set<Class<?>> res = new HashSet<>();
        res.addAll(repositories.values());
        return res;
    }
}
