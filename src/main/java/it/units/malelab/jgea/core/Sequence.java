/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

/**
 *
 * @author eric
 */
public interface Sequence<T> extends Cloneable {
  
  public T get(int index);
  public void set(int index, T t);
  public int size();
  public Sequence<T> clone();
  
}
