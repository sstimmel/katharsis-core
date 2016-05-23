package io.katharsis.queryParams;

import io.katharsis.errorhandling.exception.KatharsisException;
import io.katharsis.errorhandling.exception.QueryParseException;
import io.katharsis.jackson.exception.ParametersDeserializationException;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder responsible for building queryParams. The parameter parsing is being delegated to a parser implementation.
 * The created {@link QueryParams} object contains several fields where each of them is not-null only when
 * this parameter has been passed with a request.
 * <p>
 * ---------------------------------------------------------------------------------------------------------------------
 * POTENTIAL IMPROVEMENT NOTE : This can be made even more flexible by implementing the builder pattern to allow
 * provisioning of different parsers for each component as the QueryParamsBuilder is being built: I.e:
 * QueryParamsBuilder.builder().filters(myCustomFilterParser).sorting(myOtherCustomSortingParser)...build()
 * This way, the user can mix and match various parsing strategies for individual components.
 * QueryParamsParser could become a one method interface and this could be particularly useful to Java 8 users who
 * can simply pass instances of java.lang.Function to implement custom parsing per component (filter/sort/group/etc etc).
 */
public class QueryParamsBuilder {

    private final QueryParamsParser queryParamsParser;

    public QueryParamsBuilder(final QueryParamsParser queryParamsParser) {
        this.queryParamsParser = queryParamsParser;
    }

    /**
     * Code adapted from http://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, Set<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, Set<String>> query_pairs = new LinkedHashMap<>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedHashSet<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    /**
     * Decodes passed query parameters
     *
     * @param queryParams Map of provided query params
     * @return QueryParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public QueryParams buildQueryParams(Map<String, Set<String>> queryParams) {
        QueryParams deserializedQueryParams = new QueryParams();
        try {
            deserializedQueryParams.setFilters(this.queryParamsParser.parseFiltersParameters(queryParams));
            deserializedQueryParams.setSorting(this.queryParamsParser.parseSortingParameters(queryParams));
            deserializedQueryParams.setGrouping(this.queryParamsParser.parseGroupingParameters(queryParams));
            deserializedQueryParams.setPagination(this.queryParamsParser.parsePaginationParameters(queryParams));
            deserializedQueryParams.setIncludedFields(this.queryParamsParser.parseIncludedFieldsParameters(queryParams));
            deserializedQueryParams.setIncludedRelations(this.queryParamsParser.parseIncludedRelationsParameters(queryParams));
        } catch (KatharsisException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ParametersDeserializationException(e.getMessage());
        }
        return deserializedQueryParams;
    }

    /**
     * Decodes passed query parameters
     *
     * @param url The URL instance
     * @return QueryParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public QueryParams parseQuery(URL url) throws KatharsisException {
        try {
            return buildQueryParams(splitQuery(url));
        } catch (UnsupportedEncodingException e) {
            throw new QueryParseException(String.format("Could not parse query %s. %s", url.toString(), e.getMessage()));
        }
    }

}
