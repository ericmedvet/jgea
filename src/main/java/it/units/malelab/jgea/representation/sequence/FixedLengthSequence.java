/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.representation.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Eric Medvet <eric.medvet@gmail.com>
 */
public class FixedLengthSequence<T> implements Sequence<T> {
  
  private final List<T> values;

  public FixedLengthSequence(int n, T value) {
    if (n<1) {
      throw new IllegalArgumentException("Length must be >0");
    }
    values = new ArrayList<>(n);
    for (int i = 0; i<n; i++) {
      values.add(value);
    }
  }

  @Override
  public T get(int index) {
    return values.get(index);
  }

  @Override
  public void set(int index, T t) {
    values.set(index, t);
  }

  @Override
  public Sequence<T> clone() {
    FixedLengthSequence<T> cloned = new FixedLengthSequence<>(size(), get(0));
    for (int i = 1; i<size(); i++) {
      cloned.set(i, cloned.get(i));
    }
    return cloned;
  }

  @Override
  public int size() {
    return values.size();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(values.stream().map(Object::toString).collect(Collectors.joining(", ")));
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.values);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FixedLengthSequence<?> other = (FixedLengthSequence<?>) obj;
    if (!Objects.equals(this.values, other.values)) {
      return false;
    }
    return true;
  }
  
}
