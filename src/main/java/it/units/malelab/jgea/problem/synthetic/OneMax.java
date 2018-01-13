/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.synthetic;

import it.units.malelab.jgea.core.Problem;
import it.units.malelab.jgea.core.genotype.BitString;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import java.util.Random;

/**
 *
 * @author eric
 */
public class OneMax extends Problem<BitString, Double> {

  public OneMax(final int size) {
    super(new BoundMapper<BitString, Double>() {

      @Override
      public Double worstValue() {
        return 0d;
      }

      @Override
      public Double bestValue() {
        return 1d;
      }

      @Override
      public Double map(BitString b, Random random, Listener listener) throws MappingException {
        return (double)b.count()/(double)size;
      }
    });
  }
  
}
