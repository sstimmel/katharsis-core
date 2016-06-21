package io.katharsis.dispatcher;

import io.katharsis.domain.api.TopLevel;
import lombok.Data;

@Data
public class DefaultResponseContext implements ResponseContext {

    private int httpStatus;
    private TopLevel document;

}
