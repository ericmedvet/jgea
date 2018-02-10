/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import it.units.malelab.jgea.core.Individual;
import java.util.Comparator;

/**
 *
 * @author eric
 */
public class FitnessComparator<F extends Comparable<F>> implements Comparator<Individual<Object, Object, F>> {

  @Override
  public int compare(Individual<Object, Object, F> i1, Individual<Object, Object, F> i2) {
    return i1.getFitness().compareTo(i2.getFitness());
  }
  
}
