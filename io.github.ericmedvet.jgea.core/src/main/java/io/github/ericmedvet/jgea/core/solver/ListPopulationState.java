package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;

import java.util.List;

/**
 * @author "Eric Medvet" on 2023/10/23 for jgea
 */
public interface ListPopulationState<I extends Individual<G, S, Q>, G, S, Q> extends POCPopulationState<I, G, S, Q> {
  List<I> listPopulation();
}
