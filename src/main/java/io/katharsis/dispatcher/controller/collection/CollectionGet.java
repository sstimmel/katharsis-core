package io.katharsis.dispatcher.controller.collection;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.resource.ResourceIncludeField;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.request.path.JsonPath;
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
                         TypeParser typeParser,
                         IncludeLookupSetter fieldSetter,
                         QueryParamsBuilder queryParamsBuilder) {
        super(resourceRegistry, typeParser, fieldSetter, queryParamsBuilder);
    }

    /**
     * Check if it is a GET request for a collection of resources.
     */
    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.GET && request.getPath().isCollection();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        checkResourceExists(registryEntry, path.getResource());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getUrl());
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(request.getParameterProvider());

        JsonApiResponse response;
        if (path.getIds().isPresent()) {
            Iterable<? extends Serializable> parsedIds = request.getPath().getIds().get();
            response = resourceRepository.findAll(parsedIds, queryParams);
        } else {
            response = resourceRepository.findAll(queryParams);
        }

        includeFieldSetter.setIncludedElements(registryEntry, path.getResource(), response, queryParams);

        return new CollectionResponseContext(response, path, queryParams);
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
