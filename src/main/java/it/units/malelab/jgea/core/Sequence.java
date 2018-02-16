/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public interface Sequence<T> extends Sized, Cloneable {

  public T get(int index);

  public void set(int index, T t);

  public Sequence<T> clone();

  public static <T> Sequence<T> from(final T... ts) {
    return from((List)Arrays.asList(ts));
  }
  
  public static <T> Sequence<T> from(final List<T> list) {
    return new Sequence<T>() {
      @Override
      public T get(int index) {
        return list.get(index);
      }

      @Override
      public int size() {
        return list.size();
      }

      @Override
      public Sequence<T> clone() {
        return from(new ArrayList<T>(list));
      }

      @Override
      public void set(int index, T t) {
        throw new UnsupportedOperationException("Cannot set in read-only view of a list");
      }
    };
  }

}
