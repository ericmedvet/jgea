/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface Ranker<T> {
  
  public List<Collection<T>> rank(Collection<T> ts, Random random);
  
}
