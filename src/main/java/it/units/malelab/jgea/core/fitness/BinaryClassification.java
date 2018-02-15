/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.Node;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public abstract class BinaryClassification<C, O> extends CaseBasedFitness<C, O, Boolean, Double[]> {

  private static class Aggregator implements BoundMapper<List<Boolean>, Double[]> {
    
    private final List<Boolean> actualLabels;

    public Aggregator(List<Boolean> actualLabels) {
      this.actualLabels = actualLabels;
    }

    @Override
    public Double[] worstValue() {
      return new Double[]{1d, 1d};
    }

    @Override
    public Double[] bestValue() {
      return new Double[]{0d, 0d};
    }

    @Override
    public Double[] map(List<Boolean> predictedLabels, Random random, Listener listener) throws MappingException {
      double fp = 0;
      double fn = 0;
      double p = 0;
      double n = 0;
      for (int i = 0; i<predictedLabels.size(); i++) {
        p = p+(actualLabels.get(i)?1:0);
        n = n+(actualLabels.get(i)?0:1);
        fp = fp+((!actualLabels.get(i)&&predictedLabels.get(i))?1:0);
        fn = fn+((actualLabels.get(i)&&!predictedLabels.get(i))?1:0);
      }
      return new Double[]{fp/n, fn/p};
    }
    
  }
  
  public BinaryClassification(List<Pair<O, Boolean>> data, DeterministicMapper<Pair<C, O>, Boolean> classifier) {
    super(Pair.firsts(data), classifier, new Aggregator(Pair.seconds(data)));
  }
  
}
