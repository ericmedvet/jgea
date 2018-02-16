/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.fitness;

import it.units.malelab.jgea.core.function.BiFunction;
import it.units.malelab.jgea.core.function.Bounded;
import it.units.malelab.jgea.core.function.Function;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.util.Pair;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author eric
 */
public class BinaryClassification<C, O> extends CaseBasedFitness<C, O, Boolean, Double[]> implements Bounded<Double[]> {

  private static class Aggregator implements Function<List<Boolean>, Double[]>, Bounded<Double[]> {
    
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
    public Double[] apply(List<Boolean> predictedLabels, Listener listener) throws FunctionException {
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
  
  public BinaryClassification(List<Pair<O, Boolean>> data, BiFunction<C, O, Boolean> observationFitnessFunction) {
    super(
            data.stream().map(Pair::first).collect(Collectors.toList()),
            observationFitnessFunction,
            new Aggregator(data.stream().map(Pair::second).collect(Collectors.toList()))
    );
  }

  @Override
  public Double[] bestValue() {
    return ((Bounded<Double[]>)getAggregateFunction()).bestValue();
  }

  @Override
  public Double[] worstValue() {
    return ((Bounded<Double[]>)getAggregateFunction()).worstValue();
  }
  
}
