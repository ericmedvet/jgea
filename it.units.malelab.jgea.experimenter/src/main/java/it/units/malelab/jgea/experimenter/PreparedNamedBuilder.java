package it.units.malelab.jgea.experimenter;

import it.units.malelab.jgea.experimenter.builders.Listeners;
import it.units.malelab.jgea.experimenter.builders.Problems;
import it.units.malelab.jgea.experimenter.builders.RandomGenerators;
import it.units.malelab.jgea.experimenter.builders.Solvers;
import it.units.malelab.jnb.core.NamedBuilder;

import java.util.List;

/**
 * @author "Eric Medvet" on 2022/11/24 for jgea
 */
public class PreparedNamedBuilder {

  private final static NamedBuilder<Object> NB = NamedBuilder.empty()
      .and(List.of("ea"), NamedBuilder.empty()
          .and(List.of("randomGenerator", "rg"), NamedBuilder.fromUtilityClass(RandomGenerators.class))
          .and(List.of("problem", "p"), NamedBuilder.fromUtilityClass(Problems.class))
          .and(List.of("solver", "s"), NamedBuilder.fromUtilityClass(Solvers.class))
          .and(List.of("listener", "l"), NamedBuilder.fromUtilityClass(Listeners.class))
          .and(NamedBuilder.fromClass(Experiment.class))
          .and(NamedBuilder.fromClass(Run.class))
      );

  private PreparedNamedBuilder() {
  }

  public static NamedBuilder<Object> get() {
    return NB;
  }
}
