package io.katharsis.resource.include;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.repository.exception.RelationshipRepositoryNotFoundException;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.Generics;
import io.katharsis.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class IncludeLookupSetter {
    private static final transient Logger logger = LoggerFactory.getLogger(IncludeLookupSetter.class);

    private final ResourceRegistry resourceRegistry;

    public IncludeLookupSetter(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    private static IncludedRelationsParams findInclusions(TypedParams<IncludedRelationsParams> queryParams,
                                                          String resourceName) {
        IncludedRelationsParams includedRelationsParams = null;
        for (Map.Entry<String, IncludedRelationsParams> entry : queryParams.getParams().entrySet()) {
            if (resourceName.equals(entry.getKey())) {
                includedRelationsParams = entry.getValue();
            }
        }
        return includedRelationsParams;
    }

    public void setIncludedElements(RegistryEntry registryEntry,
                                    String resourceName,
                                    Object repositoryResource,
                                    QueryParams queryParams,
                                    RepositoryMethodParameterProvider parameterProvider) {

        Object resource = resolveResource(repositoryResource);

        if (resourceHasIncludedResources(queryParams, resource)) {
            if (isCollectionResource(resource)) {
                for (Object target : (Iterable<?>) resource) {
                    setIncludedElements(registryEntry, resourceName, target, queryParams, parameterProvider);
                }
            } else {
                IncludedRelationsParams includedRelationsParams = findInclusions(queryParams.getIncludedRelations(),
                        resourceName);
                if (includedRelationsParams != null) {
                    // iterate over all included fields
                    for (Inclusion inclusion : includedRelationsParams.getParams()) {
                        List<String> pathList = inclusion.getPathList();
                        if (!pathList.isEmpty()) {
                            getElements(registryEntry, resource, pathList, queryParams, parameterProvider);
                        }
                    }
                }
            }
        }
    }

    void getElements(RegistryEntry registryEntry, Object resource, List<String> pathList, QueryParams queryParams,
                     RepositoryMethodParameterProvider parameterProvider) {
        if (!pathList.isEmpty()) {
            // resolve field
            String underlyingFieldName = underlyingFieldName(registryEntry, pathList);

            Field field = ClassUtils.findClassField(resource.getClass(), underlyingFieldName);
            if (field == null) {
                logger.warn("Error loading relationship, couldn't find field " + underlyingFieldName);
                return;
            }
            Object property = PropertyUtils.getProperty(resource, field.getName());
            //attempt to load relationship if it's null or JsonApiLookupIncludeAutomatically.overwrite() == true
            if (shouldWeLoadRelationship(field, property)) {
                property = loadRelationship(resource, field, queryParams, parameterProvider);
                PropertyUtils.setProperty(resource, field.getName(), property);
            }

            if (property != null) {
                List<String> subPathList = pathList.subList(1, pathList.size());
                if (isCollectionResource(property)) {
                    for (Object o : ((Iterable) property)) {
                        //noinspection unchecked
                        getElements(registryEntry, o, subPathList, queryParams, parameterProvider);
                    }
                } else {
                    //noinspection unchecked
                    getElements(registryEntry, property, subPathList, queryParams, parameterProvider);
                }
            }
        }
    }

    private boolean resourceHasIncludedResources(QueryParams queryParams, Object resource) {
        return resource != null && queryParams.getIncludedRelations() != null;
    }

    private boolean isCollectionResource(Object resource) {
        return Iterable.class.isAssignableFrom(resource.getClass());
    }

    private Object resolveResource(Object repositoryResource) {
        Object resource;
        if (repositoryResource instanceof JsonApiResponse) {
            resource = ((JsonApiResponse) repositoryResource).getEntity();
        } else {
            resource = repositoryResource;
        }
        return resource;
    }

    private String underlyingFieldName(RegistryEntry registryEntry, List<String> pathList) {
        String cleanedUpName = removeSurroundingBracketsAndQuotes(pathList.get(0));
        ResourceField resourceField = registryEntry.getResourceInformation().findRelationshipFieldByName(cleanedUpName);
        return resourceField.getUnderlyingName();
    }

    private String removeSurroundingBracketsAndQuotes(String fieldName) {
        String result = removePrefix(fieldName, "[");
        result = removeSuffix(result, "]");
        return result;
    }

    private String removePrefix(String source, String prefix) {
        if (source.startsWith(prefix)) {
            return source.substring(prefix.length());
        }
        return source;
    }

    private String removeSuffix(String source, String suffix) {
        if (source.length() <= suffix.length()) {
            throw new IllegalStateException("Cannot remove " + suffix + " from " + source);
        }
        if (source.endsWith(suffix)) {
            return source.substring(0, source.length() - suffix.length());
        }
        return source;
    }

    private boolean shouldWeLoadRelationship(Field field, Object property) {
        return field.isAnnotationPresent(JsonApiLookupIncludeAutomatically.class)
                && (property == null || field.getAnnotation(JsonApiLookupIncludeAutomatically.class).overwrite());
    }

    @SuppressWarnings("unchecked")
    Object loadRelationship(Object root, Field relationshipField, QueryParams queryParams,
                            RepositoryMethodParameterProvider parameterProvider) {
        Class<?> resourceClass = getClassFromField(relationshipField);
        RegistryEntry<?> rootEntry = resourceRegistry.getEntry(root.getClass());
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceClass);

        if (rootEntry == null || registryEntry == null) {
            return null;
        }

        ResourceField rootIdField = rootEntry.getResourceInformation().getIdField();
        Serializable castedResourceId = (Serializable) PropertyUtils.getProperty(root, rootIdField.getUnderlyingName());

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(root.getClass(), resourceClass);

        try {
            RelationshipRepositoryAdapter relationshipRepositoryForClass = rootEntry
                    .getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);
            if (relationshipRepositoryForClass != null) {
                JsonApiResponse response;
                if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
                    response = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField.getName(), queryParams);
                } else {
                    response = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField.getName(), queryParams);
                }
                return response.getEntity();
            }
        } catch (RelationshipRepositoryNotFoundException e) {
            logger.debug("Relationship is not defined", e);
        }

        return null;
    }

    Class<?> getClassFromField(Field relationshipField) {
        Class<?> resourceClass;
        if (Iterable.class.isAssignableFrom(relationshipField.getType())) {
            ParameterizedType stringListType = (ParameterizedType) relationshipField.getGenericType();
            resourceClass = (Class<?>) stringListType.getActualTypeArguments()[0];
        } else {
            resourceClass = relationshipField.getType();
        }
        return resourceClass;
    }
}
