package io.katharsis.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.collection.CollectionGet;
import io.katharsis.dispatcher.controller.resource.FieldResourceGet;
import io.katharsis.dispatcher.controller.resource.FieldResourcePost;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourceDelete;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourceGet;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePatch;
import io.katharsis.dispatcher.controller.resource.RelationshipsResourcePost;
import io.katharsis.dispatcher.controller.resource.ResourceDelete;
import io.katharsis.dispatcher.controller.resource.ResourceGet;
import io.katharsis.dispatcher.controller.resource.ResourcePatch;
import io.katharsis.dispatcher.controller.resource.ResourcePost;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.java.Optional;
import io.katharsis.utils.parser.TypeParser;

/**
 * A class that can be used to integrate Katharsis with external frameworks like Jersey, Spring etc. See katharsis-rs
 * and katharsis-servlet for usage.
 */
public class RequestDispatcher {

    private final ExceptionMapperRegistry exceptionMapperRegistry;
    private final QueryParamsBuilder queryParamsBuilder;

    private CollectionGet collectionGet;

    private ResourceGet resourceGet;
    private ResourcePost resourcePost;
    private ResourcePatch resourcePatch;
    private ResourceDelete resourceDelete;

    private RelationshipsResourceGet relationshipsResourceGet;
    private RelationshipsResourcePost relationshipsResourcePost;
    private RelationshipsResourcePatch relationshipsResourcePatch;
    private RelationshipsResourceDelete relationshipsResourceDelete;

    private FieldResourceGet fieldResourceGet;
    private FieldResourcePost fieldResourcePost;


    public RequestDispatcher(ExceptionMapperRegistry exceptionMapperRegistry,
                             RepositoryMethodParameterProvider parameterProvider,
                             ResourceRegistry resourceRegistry,
                             TypeParser typeParser,
                             ObjectMapper mapper,
                             QueryParamsBuilder queryParamsBuilder) {
        this.exceptionMapperRegistry = exceptionMapperRegistry;
        this.queryParamsBuilder = queryParamsBuilder;

        IncludeLookupSetter includeLookupSetter = new IncludeLookupSetter(resourceRegistry);

        this.collectionGet = new CollectionGet(resourceRegistry, parameterProvider, typeParser,
                includeLookupSetter, queryParamsBuilder);

        this.resourceGet = new ResourceGet(resourceRegistry, parameterProvider, typeParser, includeLookupSetter,
                queryParamsBuilder);
        this.resourcePost = new ResourcePost(resourceRegistry, parameterProvider, typeParser, mapper,
                queryParamsBuilder);
        this.resourcePatch = new ResourcePatch(resourceRegistry, parameterProvider, typeParser, mapper,
                queryParamsBuilder);
        this.resourceDelete = new ResourceDelete(resourceRegistry, parameterProvider, typeParser,
                queryParamsBuilder);

        this.relationshipsResourceGet = new RelationshipsResourceGet(resourceRegistry, parameterProvider,
                typeParser, includeLookupSetter, queryParamsBuilder);
        this.relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, parameterProvider, typeParser,
                queryParamsBuilder);
        this.relationshipsResourcePatch = new RelationshipsResourcePatch(resourceRegistry, parameterProvider, typeParser,
                queryParamsBuilder);
        this.relationshipsResourceDelete = new RelationshipsResourceDelete(resourceRegistry, parameterProvider,
                typeParser,queryParamsBuilder);

        this.fieldResourcePost = new FieldResourcePost(resourceRegistry, parameterProvider, typeParser, mapper,
                queryParamsBuilder);
        this.fieldResourceGet = new FieldResourceGet(resourceRegistry, parameterProvider, typeParser, includeLookupSetter,
                queryParamsBuilder);
    }

    /**
     * Dispatch the request from a client
     *
     * @param jsonPath    built {@link JsonPath} instance which represents the URI sent in the request
     * @param requestType type of the request e.g. POST, GET, PATCH
     * @param queryParams built object containing query parameters of the request
     * @param requestBody deserialized body of the client request
     * @return the response form the Katharsis
     */
    public BaseResponseContext dispatchRequest(JsonPath jsonPath,
                                               String requestType,
                                               QueryParams queryParams,
                                               @SuppressWarnings("SameParameterValue") RequestBody requestBody) {

        try {

            /**
             * Extract informations from the request. Based on those we can route the request.
             *
             * Filter first by HTTP method.
             * After that, we can need to determine if we are in one of the situations:
             * - collection - when we have no ID on the path
             * - multiple elements - many ID's
             * - individual element - we have one ID
             * - relationship - we have ID and a relationship
             * - field - we have ID and a field name
             *
             * No extra processing needs to be done - body parsing, etc.
             */

            return handleRequest(jsonPath, requestType, queryParams, requestBody);

        } catch (Exception e) {
            Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
            if (exceptionMapper.isPresent()) {
                //noinspection unchecked
                return exceptionMapper.get()
                        .toErrorResponse(e);
            } else {
                throw e;
            }
        }
    }

    public BaseResponseContext dispatchRequest(Request request) {

        try {
            /**
             * Extract informations from the request. Based on those we can route the request.
             *
             * Filter first by HTTP method.
             * After that, we can need to determine if we are in one of the situations:
             * - collection - when we have no ID on the path
             * - multiple elements - many ID's
             * - individual element - we have one ID
             * - relationship - we have ID and a relationship
             * - field - we have ID and a field name
             *
             * No extra processing needs to be done - body parsing, etc.
             */

            switch (request.getMethod()) {
                case GET:
                    return handleGet(request);
                case POST:
                    return handlePost(request);
                case PUT:
                    return handlePut(request);
                case PATCH:
                    return handlePatch(request);
                case DELETE:
                    return handleDelete(request);
                default:
                    throw new IllegalStateException("Unsupported method " + request);
            }

        } catch (Exception e) {
            Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
            if (exceptionMapper.isPresent()) {
                //noinspection unchecked
                return exceptionMapper.get()
                        .toErrorResponse(e);
            } else {
                throw e;
            }
        }
    }

    private BaseResponseContext handleGet(Request request) {
        if (collectionGet.isAcceptable(request)) {
            return collectionGet.handle(request);
        }

        if (resourceGet.isAcceptable(request)) {
            return resourceGet.handle(request);
        }

        if (fieldResourceGet.isAcceptable(request)) {
            return fieldResourceGet.handle(request);
        }

        if (relationshipsResourceGet.isAcceptable(request)) {
            return relationshipsResourceGet.handle(request);
        }

        throw new IllegalStateException("Invalid state handling request " + request);
    }

    private BaseResponseContext handlePut(Request request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private BaseResponseContext handlePost(Request request) {
        if (resourcePost.isAcceptable(request)) {
            return resourcePost.handle(request);
        }

        if (fieldResourcePost.isAcceptable(request)) {
            return fieldResourcePost.handle(request);
        }

        if (relationshipsResourcePost.isAcceptable(request)) {
            return relationshipsResourcePost.handle(request);
        }
        throw new IllegalStateException("Illegal state while processing " + request);
    }

    private BaseResponseContext handlePatch(Request request) {
        if (resourcePatch.isAcceptable(request)) {
            return resourcePatch.handle(request);
        }

        if (relationshipsResourcePatch.isAcceptable(request)) {
            return relationshipsResourcePatch.handle(request);
        }
        throw new IllegalStateException("Illegal state while processing " + request);
    }

    private BaseResponseContext handleDelete(Request request) {
        if (resourceDelete.isAcceptable(request)) {
            return resourceDelete.handle(request);
        }

        if (relationshipsResourceDelete.isAcceptable(request)) {
            return relationshipsResourceDelete.handle(request);
        }
        throw new IllegalStateException("Illegal state while processing" + request);
    }

    protected BaseResponseContext handleRequest(JsonPath jsonPath,
                                                String requestType,
                                                QueryParams queryParams,
                                                RequestBody requestBody) {
        switch (requestType.toLowerCase()) {
            case "get":
                return handleGet(jsonPath, requestType, queryParams, requestBody);
            case "post":
                return handlePost(jsonPath, requestType, queryParams, requestBody);
            case "patch":
                return handlePatch(jsonPath, requestType, queryParams, requestBody);
            case "delete":
                return handleDelete(jsonPath, requestType, queryParams, requestBody);
            default:
                throw new MethodNotFoundException(PathBuilder.buildPath(jsonPath), requestType);
        }
    }

    protected BaseResponseContext handleGet(JsonPath jsonPath,
                                            String requestType,
                                            QueryParams queryParams,
                                            RequestBody requestBody) {
        if (collectionGet.isAcceptable(jsonPath, requestType)) {
            return collectionGet.handle(jsonPath, queryParams, requestBody);
        }

        if (resourceGet.isAcceptable(jsonPath, requestType)) {
            return resourceGet.handle(jsonPath, queryParams, requestBody);
        }

        if (fieldResourceGet.isAcceptable(jsonPath, requestType)) {
            return fieldResourceGet.handle(jsonPath, queryParams, requestBody);
        }

        if (relationshipsResourceGet.isAcceptable(jsonPath, requestType)) {
            return relationshipsResourceGet.handle(jsonPath, queryParams, requestBody);
        }

        throw new IllegalStateException("Invalid state handling GET" + PathBuilder.buildPath(jsonPath));
    }


    protected BaseResponseContext handlePost(JsonPath jsonPath,
                                             String requestType,
                                             QueryParams queryParams,
                                             RequestBody requestBody) {
        if (resourcePost.isAcceptable(jsonPath, requestType)) {
            return resourcePost.handle(jsonPath, queryParams, requestBody);
        }

        if (fieldResourcePost.isAcceptable(jsonPath, requestType)) {
            return fieldResourcePost.handle(jsonPath, queryParams, requestBody);
        }

        if (relationshipsResourcePost.isAcceptable(jsonPath, requestType)) {
            return relationshipsResourcePost.handle(jsonPath, queryParams, requestBody);
        }
        throw new IllegalStateException("Illegal state while processing POST" + PathBuilder.buildPath(jsonPath));
    }

    protected BaseResponseContext handlePatch(JsonPath jsonPath,
                                              String requestType,
                                              QueryParams queryParams,
                                              RequestBody requestBody) {
        if (resourcePatch.isAcceptable(jsonPath, requestType)) {
            return resourcePatch.handle(jsonPath, queryParams, requestBody);
        }

        if (relationshipsResourcePatch.isAcceptable(jsonPath, requestType)) {
            return relationshipsResourcePatch.handle(jsonPath, queryParams, requestBody);
        }
        throw new IllegalStateException("Illegal state while processing PATCH" + PathBuilder.buildPath(jsonPath));
    }

    protected BaseResponseContext handleDelete(JsonPath jsonPath,
                                               String requestType,
                                               QueryParams queryParams,
                                               RequestBody requestBody) {

        if (resourceDelete.isAcceptable(jsonPath, requestType)) {
            return resourceDelete.handle(jsonPath, queryParams, requestBody);
        }

        if (relationshipsResourceDelete.isAcceptable(jsonPath, requestType)) {
            return relationshipsResourceDelete.handle(jsonPath, queryParams, requestBody);
        }
        throw new IllegalStateException("Illegal state while processing DELETE" + PathBuilder.buildPath(jsonPath));
    }
}
