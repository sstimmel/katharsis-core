package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.PATCH.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                      RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {

        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);
        DataBody dataBody = dataBody(requestBody, resourceEndpointName, HttpMethod.PATCH);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);

        String idString = jsonPath.getIds().getIds().get(0);
        Serializable resourceId = parseId(endpointRegistryEntry, idString);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(resourceId, queryParams));


        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, dataBody, queryParams, parameterProvider);
        JsonApiResponse response = resourceRepository.save(resource, queryParams);

        return new ResourceResponseContext(response, jsonPath, queryParams);
    }

}
