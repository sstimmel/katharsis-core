package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonApiPath;
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

    public CollectionGet(ResourceRegistry resourceRegistry,
                         RepositoryMethodParameterProvider parameterProvider,
                         TypeParser typeParser,
                         IncludeLookupSetter fieldSetter,
                         QueryParamsBuilder queryParamsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, fieldSetter, queryParamsBuilder);
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
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && request.getPath().isCollection();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RequestBody requestBody) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        checkResourceExists(registryEntry, resourceName);

        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(getParameterProvider());
        Iterable<? extends Serializable> parsedIds = parseResourceIds(registryEntry, jsonPath);
        JsonApiResponse response = collectionResponse(resourceRepository, queryParams, parsedIds);

        includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams, getParameterProvider());

        return new CollectionResponseContext(response, jsonPath, queryParams);
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        checkResourceExists(registryEntry, path.getResource());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getUrl());

        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(getParameterProvider());

        Iterable<? extends Serializable> parsedIds = request.getPath().getIds().get();
        JsonApiResponse response;
        if (path.getIds().isPresent()) {
            response = resourceRepository.findAll(queryParams);
        } else {
            response = resourceRepository.findAll(parsedIds, queryParams);
        }

        includeFieldSetter.setIncludedElements(registryEntry, path.getResource(), response, queryParams, getParameterProvider());

        return new CollectionResponseContext(response, path, queryParams);
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
