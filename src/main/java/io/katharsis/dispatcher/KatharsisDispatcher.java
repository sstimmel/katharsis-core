package io.katharsis.dispatcher;

import io.katharsis.request.Request;

public interface KatharsisDispatcher {

    ResponseContext dispatch(Request request);
}
