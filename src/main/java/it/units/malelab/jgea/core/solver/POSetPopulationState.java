package it.units.malelab.jgea.core.solver;

import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;

import java.util.Collection;

public interface POSetPopulationState<G, S, F> extends FlatPopulationState<G, S, F> {
  PartiallyOrderedCollection<Individual<G, S, F>> getPopulation();

  @Override
  default Collection<Individual<G, S, F>> getAllIndividuals() {
    return getPopulation().all();
  }

  @Override
  default Collection<Individual<G, S, F>> getBestIndividuals() {
    return getPopulation().firsts();
  }
}
