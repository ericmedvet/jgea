/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.evolver.biased;

import com.google.common.collect.TreeMultimap;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Misc;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class PercentileProportional extends Percentile<Double> {

  public PercentileProportional(float p) {
    super(p);
  }
  
  @Override
  public Integer apply(List<List<Double>> samples, Random random, Listener listener) throws FunctionException {
    TreeMultimap<Double, Integer> map = getPercentileMap(samples);
    double max = map.keySet().stream().filter(d -> !d.isInfinite()&&!d.isNaN()).mapToDouble(Double::doubleValue).max().orElse(1d);
    double sum = map.keySet().stream().map(d -> (!d.isInfinite()&&!d.isNaN())?d:max).mapToDouble(Double::doubleValue).sum();
    List<Double> thresholds = map.keySet().stream().map(d -> (!d.isInfinite()&&!d.isNaN())?d/sum:max/sum).collect(Collectors.toList());
    Collections.reverse(thresholds); //TODO maybe make proportional on 1/f, instead
    List<Collection<Integer>> indexes = map.asMap().values().stream().collect(Collectors.toList());
    double r = random.nextDouble();
    for (int i = 0; i<thresholds.size(); i++) {
      if (r<thresholds.get(i)) {
        return Misc.pickRandomly(indexes.get(i), random);
      }
    }
    return random.nextInt(samples.size());
  }
  
  
}
