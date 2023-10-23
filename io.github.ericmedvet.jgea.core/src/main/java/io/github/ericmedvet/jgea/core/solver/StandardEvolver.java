package io.github.ericmedvet.jgea.core.solver;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.GeneticOperator;
import io.github.ericmedvet.jgea.core.order.PartiallyOrderedCollection;
import io.github.ericmedvet.jgea.core.problem.QualityBasedProblem;
import io.github.ericmedvet.jgea.core.selector.Selector;
import io.github.ericmedvet.jgea.core.solver.state.POCPopulationState;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author "Eric Medvet" on 2023/10/23 for jgea
 */
public class StandardEvolver<G, S, Q> extends AbstractStandardEvolver<
    POCPopulationState<Individual<G, S, Q>, G, S, Q>,
    Individual<G, S, Q>,
    QualityBasedProblem<S, Q>,
    G, S, Q
    > {
  public StandardEvolver(
      Function<Individual<G, S, Q>, Individual<G, S, Q>> individualBuilder,
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      int populationSize,
      Predicate<? super POCPopulationState<Individual<G, S, Q>, G, S, Q>> stopCondition,
      Map<GeneticOperator<G>, Double> operators,
      Selector<? super Individual<G, S, Q>> parentSelector,
      Selector<? super Individual<G, S, Q>> unsurvivalSelector,
      int offspringSize,
      boolean overlapping,
      boolean remap
  ) {
    super(
        individualBuilder,
        solutionMapper,
        genotypeFactory,
        populationSize,
        stopCondition,
        operators,
        parentSelector,
        unsurvivalSelector,
        offspringSize,
        overlapping,
        remap
    );
  }

  @Override
  protected POCPopulationState<Individual<G, S, Q>, G, S, Q> update(
      POCPopulationState<Individual<G, S, Q>, G, S, Q> state,
      QualityBasedProblem<S, Q> problem,
      Collection<Individual<G, S, Q>> individuals,
      long nOfBirths,
      long nOfFitnessEvaluations
  ) {
    return AbstractStandardEvolver.State.from(
        (AbstractStandardEvolver.State<Individual<G, S, Q>, G, S, Q>) state,
        progress(state),
        nOfBirths,
        nOfFitnessEvaluations,
        PartiallyOrderedCollection.from(individuals, comparator(problem))
    );
  }

  @Override
  protected POCPopulationState<Individual<G, S, Q>, G, S, Q> init(
      QualityBasedProblem<S, Q> problem,
      Collection<Individual<G, S, Q>> individuals
  ) {
    return new AbstractStandardEvolver.State<>(PartiallyOrderedCollection.from(individuals, comparator(problem)));
  }
}
