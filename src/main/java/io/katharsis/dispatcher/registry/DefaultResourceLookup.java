package io.katharsis.dispatcher.registry;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
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

    /**
     * Maps resource type (or name) to the resource class.
     */
    private Map<String, Class<?>> resources;
    /**
     * Maps the resource type to the repository that implements operations for it.
     */
    private Map<String, Class<?>> repositories;
    /**
     * Maps resource type to the relationship repository. Resource type (source) is mapped to the target resource entry.
     * <p/>
     * (source resource) -> ( target resource , target resource class )
     */
    private Map<String, Map<String, Class<?>>> relationships;

    private Reflections reflections;

    public DefaultResourceLookup(@NonNull String packages) {
        this.reflections = new Reflections(packages.split(","));

        this.resources = processResourceClasses();
        this.repositories = processRepositoryClasses();
        this.relationships = processRelationshipClasses();
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

            String resourceName = validateResource(res.value());

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

    private Map<String, Map<String, Class<?>>> processRelationshipClasses() {
        Map<String, Map<String, Class<?>>> relationships = new HashMap<>();

        for (Class relationshipRepo : findRelationshipRepositoryClasses()) {
            JsonApiRelationshipRepository res = getAnnotation(relationshipRepo, JsonApiRelationshipRepository.class);

            String source = checkRepositoryExists(validateResource(res.source()));
            String target = checkRepositoryExists(validateResource(res.target()));

            Map<String, Class<?>> repos = getOrInit(relationships, source);
            repos.put(target, res.target());
            relationships.put(source, repos);
        }

        return relationships;
    }

    private Map<String, Class<?>> getOrInit(Map<String, Map<String, Class<?>>> relationships, String source) {
        Map<String, Class<?>> repos = relationships.get(source);
        if (repos == null) {
            repos = new HashMap<>();
        }
        return repos;
    }

    private String checkRepositoryExists(@NonNull String resource) throws KatharsisInitializationException {
        if (!repositories.containsKey(resource)) {
            throw new KatharsisInitializationException("A relationship repository needs repositories for `source` " +
                    "and `target` resources. \nMissing repository for " + resource);
        }
        return resource;
    }

    private String validateResource(Class resourceClass) throws KatharsisInitializationException {
        return checkResourceIsKnown(getResourceName(resourceClass));
    }

    private String checkResourceIsKnown(String resourceName) {
        if (!resources.containsKey(resourceName)) {
            throw new KatharsisInitializationException("Resource is not known in this registry: " + resourceName);
        }
        return resourceName;
    }

    private String getResourceName(Class resourceClass) {
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
        return reflections.getTypesAnnotatedWith(JsonApiResourceRepository.class);
    }

    protected Set<Class<?>> findRelationshipRepositoryClasses() {
        return reflections.getTypesAnnotatedWith(JsonApiRelationshipRepository.class);
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
