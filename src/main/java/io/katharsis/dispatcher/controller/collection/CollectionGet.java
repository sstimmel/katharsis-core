package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

import static io.katharsis.dispatcher.controller.Utils.checkResourceExists;

public class CollectionGet extends ResourceIncludeField {

    public CollectionGet(ResourceRegistry resourceRegistry, TypeParser typeParser, IncludeLookupSetter fieldSetter) {
        super(resourceRegistry, typeParser, fieldSetter);
    }

    /**
     * Check if it is a GET request for a collection of resources.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RepositoryMethodParameterProvider
            parameterProvider, RequestBody requestBody) {

        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        checkResourceExists(registryEntry, resourceName);

        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
        Iterable<? extends Serializable> parsedIds = parseResourceIds(registryEntry, jsonPath);
        JsonApiResponse response = collectionResponse(resourceRepository, queryParams, parsedIds);

        includeFieldSetter.setIncludedElements(resourceName, response, queryParams, parameterProvider);

        return new CollectionResponseContext(response, jsonPath, queryParams);
    }

    private JsonApiResponse collectionResponse(ResourceRepositoryAdapter resourceRepository, QueryParams queryParams, Iterable<? extends Serializable> parsedIds) {
        JsonApiResponse response;
        if (parsedIds == null) {
            response = resourceRepository.findAll(queryParams);
        } else {
            response = resourceRepository.findAll(parsedIds, queryParams);
        }
        return response;
    }

    //TODO: ieugen we could reason better about this if we JSonPath had a richer API
    private Iterable<? extends Serializable> parseResourceIds(RegistryEntry registryEntry, JsonPath jsonPath) {
        if (jsonPath.doesNotHaveIds()) {
            return null;
        }

        Class<? extends Serializable> idType = (Class<? extends Serializable>) registryEntry
                .getResourceInformation().getIdField().getType();
        return typeParser.parse((Iterable<String>) jsonPath.getIds().getIds(), idType);
    }

}
