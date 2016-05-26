package io.katharsis.queryParams.params;

import io.katharsis.queryParams.include.Inclusion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;


@Getter
@ToString
@AllArgsConstructor
public class IncludedRelationsParams {

    private Set<Inclusion> params;

}
