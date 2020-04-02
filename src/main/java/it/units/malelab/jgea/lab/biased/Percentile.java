/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.lab.biased;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.util.Misc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public class Percentile<F extends Comparable> implements NonDeterministicFunction<List<List<F>>, Integer> {
  
  private final float p; //lower is better

  public Percentile(float p) {
    this.p = p;
  }

  @Override
  public Integer apply(List<List<F>> samples, Random random, Listener listener) throws FunctionException {
    Multimap<F, Integer> map = getPercentileMap(samples);
    List<Integer> indexes = new ArrayList<>(map.get(Misc.first(map.keySet())));
    return Misc.pickRandomly(indexes, random);
  }

  protected TreeMultimap<F, Integer> getPercentileMap(List<List<F>> samples) {
    //collect percentile values
    TreeMultimap<F, Integer> map = TreeMultimap.create();
    for (int i = 0; i<samples.size(); i++) {
      List<F> sample = new ArrayList<>(samples.get(i));
      Collections.sort(sample);
      F f = sample.get(Math.min(sample.size()-1, Math.max(0, Math.round((float)sample.size()*p))));
      map.put(f, i);
    }
    return map;
  }
  
  
}
