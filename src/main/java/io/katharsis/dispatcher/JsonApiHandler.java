package io.katharsis.dispatcher;

import io.katharsis.request.Request;

public interface JsonApiHandler {

    ResponseContext handle(Request request);
}
