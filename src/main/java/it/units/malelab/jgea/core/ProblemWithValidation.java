/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import it.units.malelab.jgea.core.mapper.BoundMapper;
import it.units.malelab.jgea.core.mapper.Mapper;

/**
 *
 * @author eric
 */
public interface ProblemWithValidation<S, F> extends Problem<S, F> {
  
  public Mapper<S, F> getValidationMapper();
  
}
