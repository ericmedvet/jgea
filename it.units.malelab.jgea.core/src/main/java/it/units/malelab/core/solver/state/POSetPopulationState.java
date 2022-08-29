package it.units.malelab.core.solver.state;

import it.units.malelab.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.core.order.PartialComparator;
import it.units.malelab.core.order.PartiallyOrderedCollection;
import it.units.malelab.core.solver.Individual;

import java.time.LocalDateTime;

public class POSetPopulationState<G, S, F> extends State {
  protected long nOfBirths;
  protected long nOfFitnessEvaluations;
  protected PartiallyOrderedCollection<Individual<G, S, F>> population;

  public POSetPopulationState() {
    nOfBirths = 0;
    nOfFitnessEvaluations = 0;
    population = new DAGPartiallyOrderedCollection<>((i1, i2) -> PartialComparator.PartialComparatorOutcome.SAME);
  }

  protected POSetPopulationState(
      LocalDateTime startingDateTime,
      long elapsedMillis,
      long nOfIterations,
      long nOfBirths,
      long nOfFitnessEvaluations,
      PartiallyOrderedCollection<Individual<G, S, F>> population
  ) {
    super(startingDateTime, elapsedMillis, nOfIterations);
    this.nOfBirths = nOfBirths;
    this.nOfFitnessEvaluations = nOfFitnessEvaluations;
    this.population = population;
  }

  public long getNOfBirths() {
    return nOfBirths;
  }

  public long getNOfFitnessEvaluations() {
    return nOfFitnessEvaluations;
  }

  public PartiallyOrderedCollection<Individual<G, S, F>> getPopulation() {
    return population;
  }

  public void setPopulation(PartiallyOrderedCollection<Individual<G, S, F>> population) {
    this.population = population;
  }

  @Override
  public POSetPopulationState<G, S, F> immutableCopy() {
    return new POSetPopulationState<>(
        startingDateTime,
        elapsedMillis,
        nOfIterations,
        nOfBirths,
        nOfFitnessEvaluations,
        population.immutableCopy()
    );
  }

  public void incNOfBirths(long n) {
    nOfBirths = nOfBirths + n;
  }

  public void incNOfFitnessEvaluations(long n) {
    nOfFitnessEvaluations = nOfFitnessEvaluations + n;
  }
}
