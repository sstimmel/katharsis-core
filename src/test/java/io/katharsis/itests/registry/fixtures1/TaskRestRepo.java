package io.katharsis.itests.registry.fixtures1;

import io.katharsis.repository.annotations.JsonApiResourceRepository;
import lombok.Data;


@Data
@JsonApiResourceRepository(value = Task.class)
public class TaskRestRepo {
}