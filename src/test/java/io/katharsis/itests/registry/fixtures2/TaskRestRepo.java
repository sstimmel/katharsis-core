package io.katharsis.itests.registry.fixtures2;

import io.katharsis.repository.annotations.JsonApiResourceRepository;
import lombok.Data;


/**
 * Repository defined without a real resource
 */
@Data
@JsonApiResourceRepository(value = Object.class)
public class TaskRestRepo {
}