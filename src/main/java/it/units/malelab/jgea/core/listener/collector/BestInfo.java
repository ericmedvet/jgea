/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.Sized;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.event.EvolutionEvent;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author eric
 */
public class BestInfo<F> implements DataCollector {

  private final Function<F, List<Item>> fitnessSplitter;

  public BestInfo(Function<F, List<Item>> fitnessSplitter) {
    this.fitnessSplitter = fitnessSplitter;
  }
  
  public BestInfo(Function<?, F> function, String... formats) {
    this(Item.fromMultiobjective((Function)function, formats));
  }
  
  public BestInfo(String format) {
    this((f, listener) -> Collections.singletonList(new Item<>("", f, format)));
  }
  
  @Override
  public List<Item> collect(EvolutionEvent evolutionEvent) {
    List<Collection<Individual>> rankedPopulation = new ArrayList<>((List)evolutionEvent.getRankedPopulation());
    Individual best = Misc.first(rankedPopulation.get(0));
    List<Item> items = new ArrayList<>();
    items.add(new Item<>("best.genotype.size", size(best.getGenotype()), "%4d"));
    items.add(new Item<>("best.solution.size", size(best.getSolution()), "%4d"));
    items.add(new Item<>("best.age", evolutionEvent.getIteration() - best.getBirthIteration(), "%3d"));
    for (Item fitnessItem : fitnessSplitter.apply((F)best.getFitness())) {
      items.add(fitnessItem.prefixed("best.fitness"));
    }
    return items;
  }
  
  public static Integer size(Object o) {
    if (o instanceof Sized) {
      return ((Sized)o).size();
    }
    if (o instanceof Collection) {
      if (Misc.first((Collection)o) instanceof Sized) {
        return ((Collection)o).stream().mapToInt(i -> ((Sized)i).size()).sum();
      }
      return ((Collection)o).size();
    }
    if (o instanceof String) {
      return ((String)o).length();
    }
    if (o instanceof Pair) {
      Integer firstSize = size(((Pair)o).first());
      Integer secondSize = size(((Pair)o).second());
      if ((firstSize!=null)&&(secondSize!=null)) {
        return firstSize+secondSize;
      }
    }
    return null;
  }

}
