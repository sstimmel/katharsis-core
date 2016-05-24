package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry,
                        TypeParser typeParser,
                        ObjectMapper objectMapper,
                        QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, objectMapper, paramsBuilder);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
//        return jsonPath.isCollection() &&
//                jsonPath instanceof ResourcePath &&
//                HttpMethod.POST.name()
//                        .equals(requestType);

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(Request request) {
        //        String resourceEndpointName = jsonPath.getResourceName();
//        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
//        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);
//
//        DataBody dataBody = dataBody(requestBody, resourceEndpointName, HttpMethod.POST);
//
//        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
//        verifyTypes(HttpMethod.POST, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);
//        Object newResource = ClassUtils.newInstance(bodyRegistryEntry.getResourceInformation().getResourceClass());
//
//        setId(dataBody, newResource, bodyRegistryEntry);
//        setAttributes(dataBody, newResource, bodyRegistryEntry.getResourceInformation());
//        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository();
//        setRelations(newResource, bodyRegistryEntry, dataBody, queryParams);
//        JsonApiResponse response = resourceRepository.save(newResource, queryParams);
//
//        return new ResourceResponseContext(response, jsonPath, queryParams, HttpStatus.CREATED_201);

        throw new UnsupportedOperationException("Not implemented");
    }

}
