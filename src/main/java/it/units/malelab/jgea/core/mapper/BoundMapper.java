/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

/**
 *
 * @author eric
 */
public interface BoundMapper<A, B> extends Mapper<A, B>{
  
  public B worstValue();
  public B bestValue();
  
}
