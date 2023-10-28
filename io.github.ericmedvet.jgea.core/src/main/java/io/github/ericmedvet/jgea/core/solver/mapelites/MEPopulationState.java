package io.github.ericmedvet.jgea.core.solver.mapelites;

import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import java.util.List;
import java.util.Map;

/**
 * @author "Eric Medvet" on 2023/10/28 for jgea
 */
public interface MEPopulationState<G, S, Q> extends POCPopulationState<Individual<G, S, Q>, G, S, Q> {
  Map<List<Integer>, Individual<G, S, Q>> mapOfElites();
  List<MapElites.Descriptor<G, S, Q>> descriptors();
}
