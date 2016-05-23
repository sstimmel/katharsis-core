package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.RelationshipsPath;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RelationshipsResourceGet extends ResourceIncludeField {

    public RelationshipsResourceGet(ResourceRegistry resourceRegistry,
                                    RepositoryMethodParameterProvider parameterProvider,
                                    TypeParser typeParser,
                                    IncludeLookupSetter fieldSetter,
                                    QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, fieldSetter, paramsBuilder);
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof RelationshipsPath
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public boolean isAcceptable(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RequestBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);


        String elementName = jsonPath.getElementName();
        ResourceField relationshipField = registryEntry.getResourceInformation()
                .findRelationshipFieldByName(elementName);

        Utils.checkResourceFieldExists(relationshipField, resourceName);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, getParameterProvider());

        RegistryEntry relationshipFieldEntry = resourceRegistry.getEntry(relationshipFieldClass);

        Serializable castedResourceId = parseResourceId(registryEntry, resourceIds);
        BaseResponseContext target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findManyTargets(castedResourceId, elementName, queryParams);

            includeFieldSetter.setIncludedElements(relationshipFieldEntry, resourceName, response, queryParams, getParameterProvider());

            List<LinkageContainer> dataList = getLinkages(relationshipFieldClass, relationshipFieldEntry, response);
            response.setEntity(dataList);
            target = new CollectionResponseContext(response, jsonPath, queryParams);
        } else {
            @SuppressWarnings("unchecked")
            JsonApiResponse response = relationshipRepositoryForClass
                    .findOneTarget(castedResourceId, elementName, queryParams);
            includeFieldSetter.setIncludedElements(relationshipFieldEntry, resourceName, response, queryParams, getParameterProvider());

            if (response.getEntity() != null) {
                LinkageContainer linkageContainer = getLinkage(relationshipFieldClass, relationshipFieldEntry, response);
                response.setEntity(linkageContainer);
                target = new ResourceResponseContext(response, jsonPath, queryParams);
            } else {
                target = new ResourceResponseContext(response, jsonPath, queryParams);
            }
        }

        return target;
    }

    @Override
    public BaseResponseContext handle(Request request) {
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

    private Serializable parseResourceId(RegistryEntry<?> registryEntry, PathIds resourceIds) {
        String resourceId = resourceIds.getIds().get(0);
        return parseId(registryEntry, resourceId);
    }

}
