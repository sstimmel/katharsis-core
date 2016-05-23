package io.katharsis.request;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.utils.java.Optional;

import java.io.InputStream;
import java.net.URL;

/**
 * Katharsis Domain object that holds for the request data.
 * <p>
 * The body InputStream is not closed by Katharsis.
 */
public class Request {

    private final URL url;
    private final HttpMethod method;
    private final JsonApiPath path;
    private final Optional<InputStream> body;

    public Request(URL url, String method, InputStream body) {
        this.url = url;
        this.path = JsonApiPath.parsePath(url);
        this.method = HttpMethod.parse(method);

        this.body = Optional.ofNullable(body);
    }

    public URL getUrl() {
        return url;
    }

    public JsonApiPath getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Optional<InputStream> getBody() {
        return body;
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(url.getQuery());
    }

    @Override
    public String toString() {
        return "Request{" +
                "path=" + path +
                ", method=" + method +
                '}';
    }
}
