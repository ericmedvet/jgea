/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.booleanfunction.element;

/**
 *
 * @author eric
 */
public class Constant implements Element {
  
  private final boolean value;

  public Constant(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + (this.value ? 1 : 0);
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
    final Constant other = (Constant) obj;
    if (this.value != other.value) {
      return false;
    }
    return true;
  }
  
}
