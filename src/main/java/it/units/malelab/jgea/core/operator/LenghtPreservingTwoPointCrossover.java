/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.operator;

import it.units.malelab.jgea.core.Sequence;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.Random;

/**
 *
 * @author eric
 */
public class LenghtPreservingTwoPointCrossover<S extends Sequence> implements Crossover<S> {

  @Override
  public S recombine(S s1, S s2, Random random, Listener listener) {
    S s = (S)s1.clone();
    int l1 = s1.size();
    int l2 = s2.size();
    int p1 = 0;
    int p2 = 0;
    while (p1==p2) {
      p1 = random.nextInt(Math.min(l1, l2));
      p2 = random.nextInt(Math.min(l1, l2));
    }
    for (int i = Math.min(p1, p2); i<Math.max(p1, p2); i++) {
      s.set(i, s2.get(i));
    }
    return s;
  }
  
}
