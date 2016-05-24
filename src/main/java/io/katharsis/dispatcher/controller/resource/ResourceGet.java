package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

public class ResourceGet extends ResourceIncludeField {

    public ResourceGet(ResourceRegistry resourceRegistry,
                       TypeParser typeParser,
                       IncludeLookupSetter fieldSetter,
                       QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a GET request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
        //        return !jsonPath.isCollection()
//                && jsonPath instanceof ResourcePath
//                && HttpMethod.GET.name().equals(requestType);
        return request.getMethod() == HttpMethod.GET && !request.getPath().isCollection();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Passes the request to controller method.
     */
    @Override
    public BaseResponseContext handle(Request request) {
//        String resourceName = jsonPath.getElementName();
//        PathIds resourceIds = jsonPath.getIds();
//        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
//        checkResourceExists(registryEntry, resourceName);
//
//        Serializable castedId = parseId(registryEntry, resourceIds.getIds().get(0));
//
//        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();
//        @SuppressWarnings("unchecked")
//        JsonApiResponse response = resourceRepository.findOne(castedId, queryParams);
//        includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams);
//
//        return new ResourceResponseContext(response, jsonPath, queryParams);

        throw new UnsupportedOperationException("Not implemented");
    }

}
