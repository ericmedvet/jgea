/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.function;

import java.io.Serializable;

/**
 *
 * @author eric
 */
public interface Bounded<B> extends Serializable {
  
  public B bestValue();
  public B worstValue();
  
}
