package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;

public class ResourceDelete extends BaseController {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;

    public ResourceDelete(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a DELETE request for a resource.
     */
    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return !jsonPath.isCollection()
                && jsonPath instanceof ResourcePath
                && HttpMethod.DELETE.name().equals(requestType);
    }

    @Override
    public BaseResponseContext handle(JsonPath jsonPath, QueryParams queryParams,
                                      RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
        String resourceName = jsonPath.getElementName();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
        Utils.checkResourceExists(registryEntry, resourceName);

        ResourceRepositoryAdapter repository = registryEntry.getResourceRepository(parameterProvider);

        for (Serializable id : parseResourceIds(registryEntry, jsonPath)) {
            repository.delete(id, queryParams);
        }

        //TODO: Avoid nulls - use optional
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
}
