/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.distance;

import java.io.Serializable;

/**
 *
 * @author eric
 */
public interface Distance<T> extends Serializable {
  
  public double d(T t1, T t2);
  
}
