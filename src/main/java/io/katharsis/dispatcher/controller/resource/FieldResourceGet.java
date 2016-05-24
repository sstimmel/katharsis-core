package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class FieldResourceGet extends ResourceIncludeField {

    public FieldResourceGet(ResourceRegistry resourceRegistry,
                            TypeParser typeParser,
                            IncludeLookupSetter fieldSetter,
                            QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(Request request) {
//        return !jsonPath.isCollection()
//                && FieldPath.class.equals(jsonPath.getClass())
//                && HttpMethod.GET.name().equals(requestType);
        return request.getMethod() == HttpMethod.GET && canGet(request.getPath());
    }

    private boolean canGet(JsonApiPath path) {
        if (path.getField().isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public BaseResponseContext handle(Request request) {
        //        String resourceName = jsonPath.getResourceName();
//        PathIds resourceIds = jsonPath.getIds();
//
//        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
//        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
//        String elementName = jsonPath.getElementName();
//        ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
//
//        Utils.checkResourceFieldExists(relationshipField, elementName);
//
//        Class<?> baseRelationshipFieldClass = relationshipField.getType();
//        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);
//
//        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
//                .getRelationshipRepositoryForClass(relationshipFieldClass);
//
//        BaseResponseContext target;
//
//        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
//            @SuppressWarnings("unchecked")
//            JsonApiResponse response = relationshipRepositoryForClass
//                    .findManyTargets(castedResourceId, elementName, queryParams);
//
//            includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams);
//            target = new CollectionResponseContext(response, jsonPath, queryParams);
//        } else {
//            @SuppressWarnings("unchecked")
//            JsonApiResponse response = relationshipRepositoryForClass
//                    .findOneTarget(castedResourceId, elementName, queryParams);
//            includeFieldSetter.setIncludedElements(registryEntry, resourceName, response, queryParams);
//            target = new ResourceResponseContext(response, jsonPath, queryParams);
//        }
//
//        return target;
        throw new UnsupportedOperationException("Not implemented");
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        return parseId(registryEntry, resourceId);
    }

}
