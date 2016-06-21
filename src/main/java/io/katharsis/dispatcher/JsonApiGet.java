package io.katharsis.dispatcher;

import io.katharsis.domain.CollectionResponse;
import io.katharsis.domain.SingleResponse;
import io.katharsis.query.QueryParams;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements JSON-API spec related to etching data.
 * <p/>
 * http://jsonapi.org/format/#fetching
 */
@Data
@Slf4j
public class JsonApiGet implements JsonApiHandler {

    private RepositoryRegistry registry;

    @Override
    public ResponseContext handle(Request req) {
        ResponseContext res = new DefaultResponseContext();

        JsonApiPath path = req.getPath();
        QueryParams queryParams = new QueryParams(DefaultQueryParamsParser.splitQuery(path.getQuery().orElse("")));

        // find repository for resource and call method
        Repository repository = registry.get(path.getResource());
        res.setHttpStatus(200);

        if (path.isCollection()) {
            // we could get a resource or a list of id's
            Iterable<Object> response = getCollectionResponse(repository, path, queryParams);

            res.setDocument(new CollectionResponse(response, null, null, null, null));
        } else if (path.isResource()) {
            Object response = repository.findOne(path.getIds().get().get(0), queryParams);
            res.setDocument(new SingleResponse(response, null, null, null, null));
        } else if (path.isField()) {
            // validate field is relationship, fetch repository for it and return the field (collection/single)
        } else if (path.isRelationshipResource()) {
            // validate relationship, fetch repository and return resource id('s)
        }
        return res;
    }

    private Iterable<Object> getCollectionResponse(Repository repository, JsonApiPath path, QueryParams queryParams) {
        Iterable<Object> response;
        if (path.getIds().isPresent()) {
            response = repository.findAll(path.getIds().get(), queryParams);
        } else {
            response = repository.findAll(queryParams);
        }
        return response;
    }
}
