/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.ranker.selector;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author eric
 */
@FunctionalInterface
public interface Selector<T> extends Serializable {
  
  public <K extends T> K select(List<Collection<K>> ts, Random random);
  
}
