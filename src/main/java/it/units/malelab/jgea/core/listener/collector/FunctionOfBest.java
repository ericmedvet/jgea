/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author eric
 */
public class FunctionOfBest<G, S, F> extends FirstRankIndividualInfo<G, S, F> {

  public FunctionOfBest(String prefix, IndividualDataCollector<G, S, F> collector) {
    super(
            prefix,
            (Collection<Individual<G, S, F>> individuals, Listener listener) -> Misc.first(individuals),
            collector
    );
  }

  public FunctionOfBest(String prefix, Function<S, F> function, Function<F, List<Item>> splitter) {
    this(prefix, (Individual<G, S, F> individual) -> splitter.apply(function.apply(individual.getSolution())));
  }

  public FunctionOfBest(String prefix, Function<S, F> function, List<String> names, List<String> formats) {
    this(
            prefix,
            multiCollector((Function)function, names, formats)
    );
  }
  public FunctionOfBest(String prefix, Function<S, F> function, String... formats) {
    this(prefix,
            function,
            (formats.length > 1) ? Item.fromMultiobjective((Function) function, formats) : Item.fromSingle(function, formats[0])
    );
  }
  
  private static <G1, S1, F1, F2 extends List> IndividualDataCollector<G1, S1, F1> multiCollector(Function<S1, F2> function, final List<String> names, final List<String> formats) {
    return (Individual<G1, S1, F1> individual) -> {
      F2 value = function.apply(individual.getSolution());
      List<Item> items = new ArrayList<>(value.size());
      for (int i = 0; i<value.size(); i++) {
        items.add(new Item(names.get(i % names.size()), value.get(i), formats.get(i % formats.size())));
      }
      return items;
    };
  }

}
