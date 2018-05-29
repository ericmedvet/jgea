/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.function.Function;
import java.util.Comparator;

/**
 *
 * @author eric
 */
public class FitnessComparator<F> implements Comparator<Individual<Object, Object, F>> {
  
  private final Function<F, ? extends Comparable> f;

  public FitnessComparator(Function<F, ? extends Comparable> f) {
    this.f = f;
  }

  @Override
  public int compare(Individual<Object, Object, F> i1, Individual<Object, Object, F> i2) {
    return f.apply(i1.getFitness()).compareTo(f.apply(i2.getFitness()));
  }
  
}
