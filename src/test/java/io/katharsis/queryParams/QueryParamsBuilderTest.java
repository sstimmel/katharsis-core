package io.katharsis.queryParams;

import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.queryParams.include.Inclusion;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryParamsBuilderTest {

    private Map<String, Set<String>> queryParams;
    private QueryParamsBuilder sut;

    @Before
    public void prepare() {
        queryParams = new HashMap<>();
        sut = new QueryParamsBuilder(new DefaultQueryParamsParser());
    }

    @Test
    public void onGivenFiltersBuilderShouldReturnRequestParamsWithFilters() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put("filter[users][name]", Collections.singleton("John"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getFilters()
            .getParams()
            .get("users")).isNotNull();

        assertThat(result.getFilters()
            .getParams()
            .get("users")
            .getParams()
            .get("name")).isEqualTo(Collections.singleton("John"));
    }

    @Test
    public void onGivenSortingBuilderShouldReturnRequestParamsWithSorting() throws ParametersDeserializationException {
        // GIVEN
        queryParams.put("sort[users][name]", Collections.singleton("asc"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getSorting()
            .getParams()
            .get("users")).isNotNull();

        assertThat(result.getSorting()
            .getParams()
            .get("users")
            .getParams()
            .get("name")).isEqualTo(RestrictedSortingValues.asc);

    }

    @Test
    public void onGivenGroupingBuilderShouldReturnRequestParamsWithGrouping() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("group[users]", Collections.singleton("name"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getGrouping()
            .getParams()
            .get("users")).isNotNull();

        assertThat(result.getGrouping()
            .getParams()
            .get("users")
            .getParams()
            .iterator()
            .next()).isEqualTo("name");
    }


    @Test
    public void onGivenPaginationBuilderShouldReturnRequestParamsAsCorrectType() throws
            ParametersDeserializationException {
        // GIVEN
        queryParams.put("page[offset]", Collections.singleton("0"));
        queryParams.put("page[limit]", Collections.singleton("10"));
        queryParams.put("page[size]", Collections.singleton("00"));
        queryParams.put("page[cursor]", Collections.singleton("AnOboeAndAFork"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getPaginationAsInt(RestrictedPaginationKeys.offset)).isEqualTo(0);
        assertThat(result.getPaginationAsInt(RestrictedPaginationKeys.limit)).isEqualTo(10);
        assertThat(result.getPaginationAsInt(RestrictedPaginationKeys.size)).isEqualTo(0);

        assertThat(result.getPaginationAsLong(RestrictedPaginationKeys.offset)).isEqualTo(0L);
        assertThat(result.getPaginationAsLong(RestrictedPaginationKeys.limit)).isEqualTo(10L);

        assertThat(result.getPaginationAsString(RestrictedPaginationKeys.offset)).isEqualTo("0");
        assertThat(result.getPaginationAsString(RestrictedPaginationKeys.limit)).isEqualTo("10");
        assertThat(result.getPaginationAsString(RestrictedPaginationKeys.size)).isEqualTo("00");
        assertThat(result.getPaginationAsString(RestrictedPaginationKeys.cursor)).isEqualTo("AnOboeAndAFork");
    }

    @Test(expected = NumberFormatException.class)
    public void onGivenPaginationBuilderShouldThrowExceptionWhenNotInteger() throws Exception {
        // GIVEN
        queryParams.put("page[cursor]", Collections.singleton("AnOboeAndAFork"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);
        result.getPaginationAsInt(RestrictedPaginationKeys.cursor);
    }

    @Test(expected = NumberFormatException.class)
    public void onGivenPaginationBuilderShouldThrowExceptionWhenNotLong() throws Exception {
        // GIVEN
        queryParams.put("page[cursor]", Collections.singleton("AnOboeAndAFork"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);
        result.getPaginationAsLong(RestrictedPaginationKeys.cursor);
    }

    @Test(expected = ParametersDeserializationException.class)
    public void onGivenPaginationTooDeepBuilderShouldThrowException() throws Exception {
        // GIVEN
        queryParams.put("page[cursor][whoops]", Collections.singleton("AnOboeAndAFork"));

        // WHEN
        sut.buildQueryParams(queryParams);
    }

    @Test
    public void onGivenIncludedFieldsBuilderShouldReturnRequestParamsWithIncludedFields() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("fields[users]", Collections.singleton("name"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedFields()
            .getParams()
            .get("users")).isNotNull();

        assertThat(result.getIncludedFields()
            .getParams()
            .get("users")
            .getParams()
            .iterator()
            .next()).isEqualTo("name");
    }

    @Test
    public void onGivenIncludedRelationBuilderShouldReturnRequestParamsWithIncludedRelation() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("include[special-users!@#$%^&*()_+=.]", Collections.singleton("friends"));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations()
            .getParams()
            .get("special-users!@#$%^&*()_+=.")).isNotNull();

        assertThat(result.getIncludedRelations()
            .getParams()
            .get("special-users!@#$%^&*()_+=.")
            .getParams()
            .iterator()
            .next()
            .getPath()).isEqualTo("friends");
    }

    @Test
    public void onGivenIncludedRelationsBuilderShouldReturnRequestParamsWithIncludedRelations() throws
        ParametersDeserializationException {
        // GIVEN
        queryParams.put("include[special-users]", new LinkedHashSet<>(Arrays.asList("friends", "foes")));

        // WHEN
        QueryParams result = sut.buildQueryParams(queryParams);

        // THEN
        assertThat(result.getIncludedRelations()
            .getParams()
            .get("special-users")).isNotNull();

        assertThat(result.getIncludedRelations()
            .getParams()
            .get("special-users")
            .getParams()).containsExactly(new Inclusion("friends"), new Inclusion("foes"));
    }
}
