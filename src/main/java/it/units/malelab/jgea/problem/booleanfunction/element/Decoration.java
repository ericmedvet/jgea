/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.booleanfunction.element;

import java.util.Objects;

/**
 *
 * @author eric
 */
public class Decoration implements Element {
  
  private final String string;

  public Decoration(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + Objects.hashCode(this.string);
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
    final Decoration other = (Decoration) obj;
    if (!Objects.equals(this.string, other.string)) {
      return false;
    }
    return true;
  }
  
}
