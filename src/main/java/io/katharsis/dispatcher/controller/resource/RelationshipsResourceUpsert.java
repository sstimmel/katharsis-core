package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.RelationshipsPath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.exception.RequestBodyNotFoundException;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public abstract class RelationshipsResourceUpsert extends BaseController {

    final TypeParser typeParser;
    private final ResourceRegistry resourceRegistry;
    private final RepositoryMethodParameterProvider parameterProvider;
    private final QueryParamsBuilder paramsBuilder;

    RelationshipsResourceUpsert(ResourceRegistry resourceRegistry,
                                RepositoryMethodParameterProvider parameterProvider,
                                TypeParser typeParser,
                                QueryParamsBuilder paramsBuilder) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.parameterProvider = parameterProvider;
        this.paramsBuilder = paramsBuilder;
    }

    /**
     * HTTP method name
     *
     * @return HTTP method name
     */
    protected abstract HttpMethod method();

    /**
     * Processes To-Many field
     *
     * @param resource                       source resource
     * @param relationshipEntry              {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBodies                     Data bodies with relationships
     * @param queryParams                    query params
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToManyRelationship(Object resource, RegistryEntry relationshipEntry,
                                                      String elementName, Iterable<DataBody> dataBodies, QueryParams queryParams,
                                                      RelationshipRepositoryAdapter relationshipRepositoryForClass);

    /**
     * Processes To-One field
     *
     * @param resource                       source resource
     * @param relationshipEntry              {@link Class} class of the relationship's id field
     * @param elementName                    field's name
     * @param dataBody                       Data body with a relationship
     * @param queryParams                    query params
     * @param relationshipRepositoryForClass Relationship repository
     */
    protected abstract void processToOneRelationship(Object resource, RegistryEntry relationshipEntry,
                                                     String elementName, DataBody dataBody, QueryParams queryParams,
                                                     RelationshipRepositoryAdapter relationshipRepositoryForClass);

    @Override
    public final boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && RelationshipsPath.class.equals(jsonPath.getClass())
                && method().name().equals(requestType);
    }

    @Override
    public final BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams, RequestBody requestBody) {
        String resourceName = jsonPath.getResourceName();
        PathIds resourceIds = jsonPath.getIds();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        Utils.checkResourceExists(registryEntry, resourceName);

        checkRequestBodyExists(requestBody, resourceName);

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath
                .getElementName());
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(jsonPath.getElementName());
        }
        ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(getParameterProvider());
        @SuppressWarnings("unchecked")
        JsonApiResponse response = resourceRepository.findOne(castedResourceId, queryParams);
        Object resource = extractResource(response);

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics
                .getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

        RegistryEntry relationshipEntry = resourceRegistry.getEntry(relationshipFieldClass);

        @SuppressWarnings("unchecked")
        RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
                .getRelationshipRepositoryForClass(relationshipFieldClass, getParameterProvider());

        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            if (!requestBody.isMultiple()) {
                throw new RequestBodyException(HttpMethod.POST, resourceName, "Non-multiple data in body");
            }
            Iterable<DataBody> dataBodies = requestBody.getMultipleData();
            processToManyRelationship(resource, relationshipEntry, jsonPath.getElementName(), dataBodies, queryParams,
                    relationshipRepositoryForClass);
        } else {
            if (requestBody.isMultiple()) {
                throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
            }
            DataBody dataBody = requestBody.getSingleData();
            processToOneRelationship(resource, relationshipEntry, jsonPath.getElementName(), dataBody, queryParams,
                    relationshipRepositoryForClass);
        }

        return new ResourceResponseContext(response, HttpStatus.NO_CONTENT_204);
    }

    private void checkRequestBodyExists(RequestBody requestBody, String resourceName) {
        if (requestBody == null) {
            throw new RequestBodyNotFoundException(HttpMethod.POST, resourceName);
        }
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        return parseId(registryEntry, resourceId);
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    @Override
    public RepositoryMethodParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    @Override
    public QueryParamsBuilder getQueryParamsBuilder() {
        return paramsBuilder;
    }
}
