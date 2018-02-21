/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Factory<T> extends Serializable {
  
  public List<T> build(int n, Random random);
  
}
