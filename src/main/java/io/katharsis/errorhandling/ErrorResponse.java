package io.katharsis.errorhandling;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.JsonApiResponse;

import java.util.Objects;

public final class ErrorResponse implements BaseResponseContext {

    public static final String ERRORS = "errors";

    private final Iterable<ErrorData> data;
    private final int httpStatus;

    public ErrorResponse(Iterable<ErrorData> data, int httpStatus) {
        this.data = data;
        this.httpStatus = httpStatus;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public JsonApiResponse getResponse() {
        return new JsonApiResponse()
                .setEntity(data);
    }

    @Override
    public JsonPath getJsonPath() {
        return null;
    }

    @Override
    public JsonApiPath getPath() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public QueryParams getQueryParams() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorResponse)) {
            return false;
        }
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, httpStatus);
    }
}