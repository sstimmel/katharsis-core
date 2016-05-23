package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class FieldResourceGet extends ResourceIncludeField {

    public FieldResourceGet(ResourceRegistry resourceRegistry,
                            RepositoryMethodParameterProvider parameterProvider,
                            TypeParser typeParser,
                            IncludeLookupSetter fieldSetter,
                            QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, fieldSetter, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && FieldPath.class.equals(jsonPath.getClass())
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public boolean isAcceptable(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RequestBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();

        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        String elementName = jsonPath.getElementName();
        ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);

        Utils.checkResourceFieldExists(relationshipField, elementName);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, getParameterProvider());

        BaseResponseContext target;

        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findManyTargets(castedResourceId, elementName, queryParams);

            includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams, getParameterProvider());
            target = new CollectionResponseContext(response, jsonPath, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findOneTarget(castedResourceId, elementName, queryParams);
            includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams, getParameterProvider());
            target = new ResourceResponseContext(response, jsonPath, queryParams);
        }

        return target;
    }

    @Override
    public BaseResponseContext handle(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        return parseId(registryEntry, resourceId);
    }

}
