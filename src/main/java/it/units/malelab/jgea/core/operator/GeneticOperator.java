/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.operator;

import it.units.malelab.jgea.core.function.FunctionException;
import it.units.malelab.jgea.core.function.NonDeterministicFunction;
import it.units.malelab.jgea.core.listener.Listener;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
public interface GeneticOperator<G> extends NonDeterministicFunction<List<G>, List<G>> {

  public int arity();

  public default GeneticOperator<G> andThen(GeneticOperator<G> other) {
    final GeneticOperator<G> thisOperator = this;
    return new GeneticOperator<G>() {
      @Override
      public int arity() {
        return thisOperator.arity();
      }

      @Override
      public List<G> apply(List<G> parents, Random random, Listener listener) throws FunctionException {
        List<G> intemediate = thisOperator.apply(parents, random, listener);
        if (intemediate.size() < other.arity()) {
          throw new IllegalArgumentException(String.format("Cannot apply composed operator: 2nd operator expects %d parents and found %d", other.arity(), intemediate.size()));
        }
        return other.apply(intemediate, random, listener);
      }
    };

  }

}
