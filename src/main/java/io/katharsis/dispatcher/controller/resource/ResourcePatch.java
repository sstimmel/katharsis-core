package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
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
import java.util.Map;

public class ResourcePatch extends ResourceUpsert {

    public ResourcePatch(ResourceRegistry resourceRegistry,
                         RepositoryMethodParameterProvider parameterProvider,
                         TypeParser typeParser,
                         @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper,
                         QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, objectMapper, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection() &&
                jsonPath instanceof ResourcePath &&
                HttpMethod.PATCH.name().equals(requestType);
    }

    @Override
    public boolean isAcceptable(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                      RequestBody requestBody) {

        String resourceEndpointName = jsonPath.getResourceName();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);
        DataBody dataBody = dataBody(requestBody, resourceEndpointName, HttpMethod.PATCH);

        RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(dataBody.getType());
        verifyTypes(HttpMethod.PATCH, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);

        String idString = jsonPath.getIds().getIds().get(0);
        Serializable resourceId = parseId(endpointRegistryEntry, idString);

        ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(getParameterProvider());
        @SuppressWarnings("unchecked")
        Object resource = extractResource(resourceRepository.findOne(resourceId, queryParams));

        String attributesFromFindOne = null;
        try {
            // extract attributes from find one without any manipulation by query params (such as sparse fieldsets)
            attributesFromFindOne = this.extractAttributesFromResourceAsJson(resource, jsonPath, new QueryParams());
            Map<String,Object> attributesToUpdate = objectMapper.readValue(attributesFromFindOne, Map.class);
            // get the JSON form the request and deserialize into a map
            String attributesAsJson = objectMapper.writeValueAsString(dataBody.getAttributes());
            Map<String,Object> attributesFromRequest = objectMapper.readValue(attributesAsJson, Map.class);;
            // walk the source map and apply target values from request
            updateValues(attributesToUpdate, attributesFromRequest);
            JsonNode upsertedAttributes = objectMapper.valueToTree(attributesToUpdate);
            dataBody.setAttributes(upsertedAttributes);
        } catch (Exception e) {
            attributesFromFindOne = "";
        }

        setAttributes(dataBody, resource, bodyRegistryEntry.getResourceInformation());
        setRelations(resource, bodyRegistryEntry, dataBody, queryParams, getParameterProvider());
        JsonApiResponse response = resourceRepository.save(resource, queryParams);

        return new ResourceResponseContext(response, jsonPath, queryParams);
    }

    @Override
    public BaseResponseContext handle(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private String extractAttributesFromResourceAsJson(Object resource, JsonPath jsonPath, QueryParams queryParams) throws Exception {

        JsonApiResponse response = new JsonApiResponse();
        response.setEntity(resource);
        ResourceResponseContext katharsisResponse = new ResourceResponseContext(response, jsonPath, queryParams);
        // deserialize using the objectMapper so it becomes json-api
        String newRequestBody = objectMapper.writeValueAsString(katharsisResponse);
        JsonNode node = objectMapper.readTree(newRequestBody);
        JsonNode attributes = node.findValue("attributes");
        return objectMapper.writeValueAsString(attributes);

    }

    private void updateValues(Map<String, Object> source,
                    Map<String, Object> updates) {

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (!updates.containsKey(entry.getKey())) {
                continue;
            }
            Object obj = entry.getValue();
            Object upd = updates.get(entry.getKey());
            if (obj instanceof Map) {
                updateValues((Map<String, Object>)obj, (Map<String, Object>)upd);
                continue;
            }
            source.put(entry.getKey(), upd);
        }

    }

}
