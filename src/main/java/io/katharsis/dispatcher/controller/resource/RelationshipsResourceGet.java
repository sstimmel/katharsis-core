package io.katharsis.dispatcher.controller.resource;

import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RelationshipsResourceGet extends ResourceIncludeField {

    public RelationshipsResourceGet(ResourceRegistry resourceRegistry,
                                    TypeParser typeParser,
                                    IncludeLookupSetter fieldSetter,
                                    QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, typeParser, fieldSetter, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(Request request) {
        //        return !jsonPath.isCollection()
//                && jsonPath instanceof RelationshipsPath
//                && HttpMethod.GET.name().equals(requestType);

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(Request request) {
        //        String resourceName = jsonPath.getResourceName();
//        PathIds resourceIds = jsonPath.getIds();
//        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);
//
//
//        String elementName = jsonPath.getElementName();
//        ResourceField relationshipField = registryEntry.getResourceInformation()
//                .findRelationshipFieldByName(elementName);
//
//        Utils.checkResourceFieldExists(relationshipField, resourceName);
//
//        Class<?> baseRelationshipFieldClass = relationshipField.getType();
//        Class<?> relationshipFieldClass = Generics
//                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);
//
//        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
//                .getRelationshipRepositoryForClass(relationshipFieldClass);
//
//        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);
//
//        Serializable castedResourceId = parseResourceId(registryEntry, resourceIds);
//        BaseResponseContext target;
//        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
//            @SuppressWarnings("unchecked")
//            JsonApiResponse response = relationshipRepositoryForClass
//                    .findManyTargets(castedResourceId, elementName, queryParams);
//
//            includeFieldSetter.setIncludedElements(relationshipFieldEntry, resourceName, response, queryParams);
//
//            List<LinkageContainer> dataList = getLinkages(relationshipFieldClass, relationshipFieldEntry, response);
//            response.setEntity(dataList);
//            target = new CollectionResponseContext(response, jsonPath, queryParams);
//        } else {
//            @SuppressWarnings("unchecked")
//            JsonApiResponse response = relationshipRepositoryForClass
//                    .findOneTarget(castedResourceId, elementName, queryParams);
//            includeFieldSetter.setIncludedElements(relationshipFieldEntry, resourceName, response, queryParams);
//
//            if (response.getEntity() != null) {
//                LinkageContainer linkageContainer = getLinkage(relationshipFieldClass, relationshipFieldEntry, response);
//                response.setEntity(linkageContainer);
//                target = new ResourceResponseContext(response, jsonPath, queryParams);
//            } else {
//                target = new ResourceResponseContext(response, jsonPath, queryParams);
//            }
//        }
//
//        return target;

        throw new UnsupportedOperationException("Not implemented");
    }

    private LinkageContainer getLinkage(Class<?> relationshipFieldClass, RegistryEntry relationshipFieldEntry, Object targetObject) {
        if (targetObject instanceof JsonApiResponse) {
            return new LinkageContainer(((JsonApiResponse) targetObject).getEntity(), relationshipFieldClass, relationshipFieldEntry);
        } else {
            return new LinkageContainer(targetObject, relationshipFieldClass, relationshipFieldEntry);
        }
    }

    private List<LinkageContainer> getLinkages(Class<?> relationshipFieldClass, RegistryEntry relationshipFieldEntry,
                                               JsonApiResponse targetObjects) {
        List<LinkageContainer> dataList = new ArrayList<>();
        if (targetObjects == null) {
            return dataList;
        }
        Iterable resources = (Iterable) targetObjects.getEntity();

        for (Object resource : resources) {
            dataList.add(new LinkageContainer(resource, relationshipFieldClass, relationshipFieldEntry));
        }
        return dataList;
    }

//    private Serializable parseResourceId(RegistryEntry<?> registryEntry, PathIds resourceIds) {
//        String resourceId = resourceIds.getIds().get(0);
//        return parseId(registryEntry, resourceId);
//    }

}
