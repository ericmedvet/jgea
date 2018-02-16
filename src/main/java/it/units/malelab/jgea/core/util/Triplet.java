/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Triplet<F, S, T> extends Pair<F, S> {

  private final T third;

  protected Triplet(F first, S second, T third) {
    super(first, second);
    this.third = third;
  }

  public T getThird() {
    return third;
  }

  public static <F, S, T> Triplet<F, S, T> build(F first, S second, T third) {
    return new Triplet<>(first, second, third);
  }

  public static <F, S, T> List<T> thirds(List<Triplet<F, S, T>> triplets) {
    List<T> thirds = new ArrayList<>(triplets.size());
    for (Triplet<F, S, T> third : triplets) {
      thirds.add(third.getThird());
    }
    return thirds;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.third);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
    if (!Objects.equals(this.third, other.third)) {
      return false;
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return "<" + first() + ", " + second() + ", " + third + '>';
  }

}
