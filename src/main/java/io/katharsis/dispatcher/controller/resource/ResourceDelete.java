package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class ResourceDelete extends BaseController {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;
    private final QueryParamsBuilder queryParamsBuilder;

    public ResourceDelete(ResourceRegistry resourceRegistry,
                          TypeParser typeParser,
                          QueryParamsBuilder paramsBuilder) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.queryParamsBuilder = paramsBuilder;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a DELETE request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
        //        return !jsonPath.isCollection()
//                && jsonPath instanceof ResourcePath
//                && HttpMethod.DELETE.name().equals(requestType);
        return request.getMethod() == HttpMethod.DELETE && canDelete(request.getPath());
    }

    /**
     * Can delete only for URLs like:
     * - http://host.local/tasks/1
     * - http://host.local/tasks/1/relationships/project
     *
     * @return
     */

    protected boolean canDelete(JsonApiPath path) {
        if (path.isCollection()) {
            return false;
        }
        if (path.getIds().isPresent()) {
            if (path.getIds().get().size() > 1) {
                return false;
            }
            if (path.getField().isPresent()) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public BaseResponseContext handle(Request request) {
        //        String resourceName = jsonPath.getElementName();
//        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
//        Utils.checkResourceExists(registryEntry, resourceName);
//
//        ResourceRepositoryAdapter repository = registryEntry.getResourceRepository();
//
//        for (Serializable id : parseResourceIds(registryEntry, jsonPath)) {
//            repository.delete(id, queryParams);
//        }
//
//        throw new UnsupportedOperationException("Not implemented");

//        //TODO: Avoid nulls - use optional
        return null;
    }

    private Iterable<? extends Serializable> parseResourceIds(RegistryEntry registryEntry, JsonPath jsonPath) {
        if (jsonPath.doesNotHaveIds()) {
            return null;
        }

        return parseIds(registryEntry, (Iterable<String>) jsonPath.getIds().getIds());
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    @Override
    public QueryParamsBuilder getQueryParamsBuilder() {
        return queryParamsBuilder;
    }

}
