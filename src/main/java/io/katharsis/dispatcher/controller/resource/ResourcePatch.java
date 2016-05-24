package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry,
                         TypeParser typeParser,
                         @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper,
                         QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, objectMapper, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(Request request) {
        //        return !jsonPath.isCollection() &&
//                jsonPath instanceof ResourcePath &&
//                HttpMethod.PATCH.name().equals(requestType);

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(Request request) {
//        String resourceEndpointName = jsonPath.getResourceName();
//        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
//        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);
//        DataBody dataBody = dataBody(requestBody, resourceEndpointName, HttpMethod.PATCH);
//
//        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
//        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);
//
//        String idString = jsonPath.getIds().getIds().get(0);
//        Serializable resourceId = parseId(endpointRegistryEntry, idString);
//
//        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository();
//        @SuppressWarnings("unchecked")
//        Object resource = extractResource(resourceRepository.findOne(resourceId, queryParams));
//
//
//        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
//        setRelations(resource, bodyRegistryEntry, dataBody, queryParams);
//        JsonApiResponse response = resourceRepository.save(resource, queryParams);
//
//        return new ResourceResponseContext(response, jsonPath, queryParams);

        throw new UnsupportedOperationException("Not implemented");
    }

}
