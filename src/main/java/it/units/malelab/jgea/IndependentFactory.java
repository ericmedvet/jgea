/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea;

import it.units.malelab.jgea.core.Factory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class IndependentFactory<T> implements Factory<T> {

  @Override
  public List<T> build(int n, Random random) {
    List<T> ts = new ArrayList<>();
    for (int i = 0; i<n; i++) {
      ts.add(build(random));
    }
    return ts;
  }
  
  protected abstract T build(Random random);
  
}
