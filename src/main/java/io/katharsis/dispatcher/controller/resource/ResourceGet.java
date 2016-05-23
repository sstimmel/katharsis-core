package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

import static io.katharsis.dispatcher.controller.Utils.checkResourceExists;

public class ResourceGet extends ResourceIncludeField {

    public ResourceGet(ResourceRegistry resourceRegistry,
                       RepositoryMethodParameterProvider parameterProvider,
                       TypeParser typeParser,
                       IncludeLookupSetter fieldSetter,
                       QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, fieldSetter, paramsBuilder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a GET request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public boolean isAcceptable(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Passes the request to controller method.
     */
    @Override
    public BaseResponseContext handle(JsonPath jsonPath,
                                      QueryParams queryParams,
                                      RequestBody requestBody) {

        String resourceName = jsonPath.getElementName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        checkResourceExists(registryEntry, resourceName);

        Serializable castedId = parseId(registryEntry, resourceIds.getIds().get(0));

        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(getParameterProvider());
        @SuppressWarnings("unchecked")
        JsonApiResponse response = resourceRepository.findOne(castedId, queryParams);
        includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams, getParameterProvider());

        return new ResourceResponseContext(response, jsonPath, queryParams);
    }

    @Override
    public BaseResponseContext handle(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
