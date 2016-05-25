package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry,
                         TypeParser typeParser,
                         QueryParamsBuilder paramsBuilder,
                         ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, paramsBuilder, objectMapper);
    }

    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.PATCH && request.getPath().isResource();
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(endpointRegistryEntry, path.getResource());
        DataBody dataBody = dataBody(request);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.PATCH, path.getResource(), endpointRegistryEntry, bodyRegistryEntry);

        String idString = path.getIds().get().get(0);
        Serializable resourceId = parseId(endpointRegistryEntry, idString);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(path.getQuery());

        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(resourceId, queryParams));


        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, dataBody, queryParams);
        JsonApiResponse response = resourceRepository.save(resource, queryParams);

        return new ResourceResponseContext(response, path, queryParams);
    }

}
