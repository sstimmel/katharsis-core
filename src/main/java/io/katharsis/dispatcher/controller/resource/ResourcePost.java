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
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.parser.TypeParser;

public class ResourcePost extends ResourceUpsert {

    public ResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Check if it is a POST request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.POST.name()
                        .equals(requestType);
    }

    @Override
    public ResourceResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                          RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);

        DataBody dataBody = dataBody(requestBody, resourceEndpointName, HttpMethod.POST);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.POST, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);
        Object newResource = ClassUtils.newInstance(bodyRegistryEntry.getResourceInformation().getResourceClass());

        setId(dataBody, newResource, bodyRegistryEntry.getResourceInformation());
        setAttributes(dataBody, newResource, bodyRegistryEntry.getResourceInformation());
        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
        setRelations(newResource, bodyRegistryEntry, dataBody, queryParams, parameterProvider);
        JsonApiResponse response = resourceRepository.save(newResource, queryParams);

        return new ResourceResponseContext(response, jsonPath, queryParams, HttpStatus.CREATED_201);
    }

}
