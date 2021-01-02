package it.units.malelab.jgea.core.listener.collector2;

import it.units.malelab.jgea.core.Individual;

import java.util.function.Function;

/**
 * @author eric on 2021/01/02 for jgea
 */
public interface IndividualCollector<G, S, F, O> extends Function<Individual<? extends G, ? extends S, ? extends F>, O> {

  String getDefaultFormat();

  String getName();

  static <F> IndividualCollector<Object, Object, F, F> fitness(String format) {
    return new IndividualCollector<Object, Object, F, F>() {
      @Override
      public String getDefaultFormat() {
        return format;
      }

      @Override
      public String getName() {
        return "fitness";
      }

      @Override
      public F apply(Individual<?, ?, ? extends F> individual) {
        return individual.getFitness();
      }
    };
  }
}
