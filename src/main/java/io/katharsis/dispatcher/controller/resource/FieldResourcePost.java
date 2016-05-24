package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

    public FieldResourcePost(ResourceRegistry resourceRegistry,
                             TypeParser typeParser,
                             @SuppressWarnings
                                     ("SameParameterValue") ObjectMapper objectMapper,
                             QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, objectMapper, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(Request request) {
        //        return !jsonPath.isCollection()
//                && FieldPath.class.equals(jsonPath.getClass())
//                && HttpMethod.POST.name()
//                .equals(requestType);

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(Request request) {

//        String resourceEndpointName = jsonPath.getResourceName();
//        PathIds resourceIds = jsonPath.getIds();
//        RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
//        Utils.checkResourceExists(endpointRegistryEntry, resourceEndpointName);
//
//        if (requestBody == null) {
//            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
//        }
//        if (requestBody.isMultiple()) {
//            throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
//        }
//
//        Serializable castedResourceId = getResourceId(resourceIds, endpointRegistryEntry);
//        ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
//                .findRelationshipFieldByName(jsonPath.getElementName());
//
//        Utils.checkResourceFieldExists(relationshipField, jsonPath.getElementName());
//
//        Class<?> baseRelationshipFieldClass = relationshipField.getType();
//        Class<?> relationshipFieldClass = Generics
//                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);
//
//        RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipFieldClass);
//        String relationshipResourceType = resourceRegistry.getResourceType(relationshipFieldClass);
//
//        DataBody dataBody = requestBody.getSingleData();
//        Object resource = buildNewResource(relationshipRegistryEntry, dataBody, relationshipResourceType);
//        setAttributes(dataBody, resource, relationshipRegistryEntry.getResourceInformation());
//        ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository();
//        JsonApiResponse savedResourceResponse = resourceRepository.save(resource, queryParams);
//        saveRelations(queryParams, extractResource(savedResourceResponse), relationshipRegistryEntry, dataBody);
//
//        Serializable resourceId = (Serializable) PropertyUtils
//                .getProperty(extractResource(savedResourceResponse), relationshipRegistryEntry.getResourceInformation()
//                        .getIdField()
//                        .getUnderlyingName());
//
//        RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
//                .getRelationshipRepositoryForClass(relationshipFieldClass);
//
//        @SuppressWarnings("unchecked")
//        JsonApiResponse parent = endpointRegistryEntry.getResourceRepository()
//                .findOne(castedResourceId, queryParams);
//        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
//            //noinspection unchecked
//            relationshipRepositoryForClass.addRelations(parent.getEntity(), Collections.singletonList(resourceId), jsonPath
//                    .getElementName(), queryParams);
//        } else {
//            //noinspection unchecked
//            relationshipRepositoryForClass.setRelation(parent.getEntity(), resourceId, jsonPath.getElementName(), queryParams);
//        }
//        return new ResourceResponseContext(savedResourceResponse, jsonPath, queryParams, HttpStatus.CREATED_201);

        throw new UnsupportedOperationException("Not implemented");
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        return parseId(registryEntry, resourceId);
    }

}
