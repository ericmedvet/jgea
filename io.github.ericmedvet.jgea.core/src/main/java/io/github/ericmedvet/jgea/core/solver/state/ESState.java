package io.github.ericmedvet.jgea.core.solver.state;

import io.github.ericmedvet.jgea.core.solver.Individual;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/10/22 for jgea
 */
public interface ESState<I extends Individual<List<Double>, S, Q>, S, Q>
    extends POSetPopulationState<I, List<Double>, S, Q> {
  List<Double> means();
}
