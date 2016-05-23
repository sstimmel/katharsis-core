package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.RelationshipRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RelationshipsResourcePost extends RelationshipsResourceUpsert {

    public RelationshipsResourcePost(ResourceRegistry resourceRegistry,
                                     RepositoryMethodParameterProvider parameterProvider,
                                     TypeParser typeParser,
                                     QueryParamsBuilder paramsBuilder) {
        super(resourceRegistry, parameterProvider, typeParser, paramsBuilder);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public void processToManyRelationship(Object resource, RegistryEntry relationshipEntry,
                                          String elementName, Iterable<DataBody> dataBodies, QueryParams queryParams,
                                          RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        List<Serializable> parsedIds = new LinkedList<>();

        for (DataBody dataBody : dataBodies) {
            Serializable parsedId = parseId(relationshipEntry, dataBody.getId());
            parsedIds.add(parsedId);
        }

        //noinspection unchecked
        relationshipRepositoryForClass.addRelations(resource, parsedIds, elementName, queryParams);
    }

    @Override
    protected void processToOneRelationship(Object resource, RegistryEntry relationshipEntry,
                                            String elementName, DataBody dataBody, QueryParams queryParams,
                                            RelationshipRepositoryAdapter relationshipRepositoryForClass) {
        Serializable parsedId = null;
        if (dataBody != null) {
            parsedId = parseId(relationshipEntry, dataBody.getId());
        }
        //noinspection unchecked
        relationshipRepositoryForClass.setRelation(resource, parsedId, elementName, queryParams);
    }

    @Override
    public boolean isAcceptable(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BaseResponseContext handle(Request request) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
