/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.util;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Pair<F, S> implements Serializable {
  
  private final F first;
  private final S second;

  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + Objects.hashCode(this.first);
    hash = 83 * hash + Objects.hashCode(this.second);
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
    final Pair<?, ?> other = (Pair<?, ?>) obj;
    if (!Objects.equals(this.first, other.first)) {
      return false;
    }
    if (!Objects.equals(this.second, other.second)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "<" + first + ", " + second + '>';
  }
  
}
