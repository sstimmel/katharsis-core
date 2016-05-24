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
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
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
                             ResourceRegistry resourceRegistry,
                             TypeParser typeParser,
                             ObjectMapper mapper,
                             QueryParamsBuilder queryParamsBuilder) {
        this.exceptionMapperRegistry = exceptionMapperRegistry;

        IncludeLookupSetter includeLookupSetter = new IncludeLookupSetter(resourceRegistry);

        this.collectionGet = new CollectionGet(resourceRegistry, typeParser, includeLookupSetter, queryParamsBuilder);

        this.resourceGet = new ResourceGet(resourceRegistry, typeParser, includeLookupSetter,
                queryParamsBuilder);
        this.resourcePost = new ResourcePost(resourceRegistry, typeParser, mapper,
                queryParamsBuilder);
        this.resourcePatch = new ResourcePatch(resourceRegistry, typeParser, mapper,
                queryParamsBuilder);
        this.resourceDelete = new ResourceDelete(resourceRegistry, typeParser,
                queryParamsBuilder);

        this.relationshipsResourceGet = new RelationshipsResourceGet(resourceRegistry,
                typeParser, includeLookupSetter, queryParamsBuilder);
        this.relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, typeParser,
                queryParamsBuilder);
        this.relationshipsResourcePatch = new RelationshipsResourcePatch(resourceRegistry, typeParser,
                queryParamsBuilder);
        this.relationshipsResourceDelete = new RelationshipsResourceDelete(resourceRegistry,
                typeParser, queryParamsBuilder);

        this.fieldResourcePost = new FieldResourcePost(resourceRegistry, typeParser, mapper,
                queryParamsBuilder);
        this.fieldResourceGet = new FieldResourceGet(resourceRegistry, typeParser, includeLookupSetter,
                queryParamsBuilder);
    }

    /**
     * Dispatch the request from a client
     *
     * @param request - the request we need to process
     * @return the response form the Katharsis
     */
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

}
