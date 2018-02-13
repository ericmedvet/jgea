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
public enum Operator implements Element {
  
  AND(".and"),
  AND1NOT(".and1not"),
  OR(".or"),
  XOR(".xor"),
  NOT(".not"),
  IF(".if");
  
  private final String string;

  private Operator(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
  
}
