package it.units.malelab.jgea.core.solver;

import java.util.Collection;

public interface FlatPopulationState<G, S, F> {
  Collection<Individual<G, S, F>> getAllIndividuals();

  Collection<Individual<G, S, F>> getBestIndividuals();
}
