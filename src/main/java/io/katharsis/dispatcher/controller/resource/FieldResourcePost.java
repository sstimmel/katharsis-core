package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser, @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper) {
        super(resourceRegistry, typeParser, objectMapper);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && FieldPath.class.equals(jsonPath.getClass())
                && HttpMethod.POST.name().equals(requestType);
    }

    @Override
    public ResourceResponse handle(JsonPath jsonPath, QueryParams queryParams,
                                   RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException,
        IOException {
        String resourceEndpointName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);

        if (endpointRegistryEntry == null) {
            throw new ResourceNotFoundException(resourceEndpointName);
        }
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
        }
        if (requestBody.isMultiple()) {
            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
        }

        Serializable castedResourceId = getResourceId(resourceIds, endpointRegistryEntry);
        ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
            .findRelationshipFieldByName(jsonPath.getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
            .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipFieldClass);
        String relationshipResourceType = resourceRegistry.getResourceType(relationshipFieldClass);

        DataBody dataBody = requestBody.getSingleData();
        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
        setId(dataBody, resource, relationshipRegistryEntry.getResourceInformation());
        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());

        FieldRepository fieldRepositoryForClass = endpointRegistryEntry.getFieldRepositoryForClass(relationshipFieldClass, parameterProvider);
        Object savedField = fieldRepositoryForClass.addField(castedResourceId, resource, relationshipField.getName(), queryParams);

        MetaInformation metaInformation = getMetaInformation(fieldRepositoryForClass,
            Collections.singletonList(savedField), queryParams);
        LinksInformation linksInformation =
            getLinksInformation(fieldRepositoryForClass, Collections.singletonList(savedField), queryParams);

        return new ResourceResponse(savedField, jsonPath, queryParams, metaInformation, linksInformation,
            HttpStatus.CREATED_201);
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        @SuppressWarnings("unchecked")
        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
