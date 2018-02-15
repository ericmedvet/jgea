/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author eric
 */
public class Pair<F, S> implements Serializable {

  private final F first;
  private final S second;

  protected Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public static <F, S> Pair<F, S> build(F first, S second) {
    return new Pair<>(first, second);
  }

  public static <F, S> List<F> firsts(List<Pair<F, S>> pairs) {
    List<F> firsts = new ArrayList<>(pairs.size());
    for (Pair<F, S> pair : pairs) {
      firsts.add(pair.getFirst());
    }
    return firsts;
  }

  public static <F, S> List<S> seconds(List<Pair<F, S>> pairs) {
    List<S> seconds = new ArrayList<>(pairs.size());
    for (Pair<F, S> pair : pairs) {
      seconds.add(pair.getSecond());
    }
    return seconds;
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
