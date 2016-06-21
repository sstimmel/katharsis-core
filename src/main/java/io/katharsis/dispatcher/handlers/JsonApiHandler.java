package io.katharsis.dispatcher.handlers;

import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.request.Request;

public interface JsonApiHandler {

    ResponseContext handle(Request request);
}
