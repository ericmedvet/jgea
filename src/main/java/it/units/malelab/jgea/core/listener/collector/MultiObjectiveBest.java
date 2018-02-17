/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.collector;

import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.util.Pair;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class MultiObjectiveBest<S, F> extends Best<Object, S, List<F>> {

  private final List<Pair<String, String>> formatPairs;
  private final Map<String, String> fitnessFormattedNames;

  public MultiObjectiveBest(List<Pair<String, String>> formatPairs, Function<S, List<F>> validationFitnessMapper) {
    super(validationFitnessMapper);
    this.formatPairs = formatPairs;
    fitnessFormattedNames = formatPairs.stream().collect(Collectors.toMap(Pair::first, Pair::second));
  }
 
 @Override
  protected Map<String, String> getFitnessFormattedNames() {
    return fitnessFormattedNames;
  }

  @Override
  protected Map<String, Object> getFitnessIndexes(List<F> fitness) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i<formatPairs.size(); i++) {
      map.put(formatPairs.get(i).first(), fitness.get(i));
    }
    return map;
  }

}
