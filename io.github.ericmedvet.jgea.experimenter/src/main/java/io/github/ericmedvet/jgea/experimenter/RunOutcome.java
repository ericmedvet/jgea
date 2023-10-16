
package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jnb.core.Param;

import java.util.List;

public record RunOutcome(
    @Param("index") String index,
    @Param("run") Run<?, ?, ?, ?> run,
    @Param("serializedGenotypes") List<String> serializedGenotypes
) {
}
