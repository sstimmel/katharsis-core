package io.katharsis.request;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.errorhandling.GenericKatharsisException;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.utils.java.Optional;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
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

    private final RepositoryMethodParameterProvider parameterProvider;

    public Request(String uri, String method, InputStream body, RepositoryMethodParameterProvider parameterProvider) {
        this(parseURL(uri), method, body, parameterProvider);
    }

    public Request(URL url, String method, InputStream body, RepositoryMethodParameterProvider parameterProvider) {
        this.url = url;
        this.path = JsonApiPath.parsePath(url);
        this.method = HttpMethod.parse(method);

        this.body = Optional.ofNullable(body);
        this.parameterProvider = parameterProvider;
    }

    private static URL parseURL(String uri) throws GenericKatharsisException {
        try {
            return URI.create(uri).toURL();
        } catch (MalformedURLException e) {
            throw new GenericKatharsisException("Invalid URL " + e.getMessage());
        }
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


    public RepositoryMethodParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    @Override
    public String toString() {
        return "Request{" +
                "path=" + path +
                ", method=" + method +
                '}';
    }
}
