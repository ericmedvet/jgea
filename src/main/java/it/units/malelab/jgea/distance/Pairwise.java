/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class Pairwise<T> implements Distance<List<T>> {

  private final Distance<T> innerDistance;

  public Pairwise(Distance<T> innerDistance) {
    this.innerDistance = innerDistance;
  }

  @Override
  public Double apply(List<T> l1, List<T> l2, Listener listener) throws FunctionException {
    List<Double> distances = new ArrayList<>();
    for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
      distances.add(innerDistance.apply(l1.get(i), l2.get(i)));
    }
    return distances.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
  }

}
