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
import it.units.malelab.jgea.core.mapper.DeterministicMapper;
import it.units.malelab.jgea.core.mapper.MappingException;
import java.util.Random;

/**
 *
 * @author eric
 */
public class OneMax implements Problem<BitString, Double> {

  private static class FitnessMapper extends DeterministicMapper<BitString, Double> implements BoundMapper<BitString, Double> {

    @Override
    public Double worstValue() {
      return 1d;
    }

    @Override
    public Double bestValue() {
      return 0d;
    }

    @Override
    public Double map(BitString b, Listener listener) throws MappingException {
      return 1d - (double) b.count() / (double) b.size();
    }

  }

  private final BoundMapper<BitString, Double> fitnessMapper;

  public OneMax() {
    this.fitnessMapper = new FitnessMapper();
  }
  
  @Override
  public BoundMapper<BitString, Double> getFitnessMapper() {
    return fitnessMapper;
  }

}
