/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.problem.symbolicregression.element;

/**
 *
 * @author eric
 */
public enum Operator implements Element {
  
  ADDITION("+"),
  SUBTRACTION("-"),
  DIVISION("/"),
  PROT_DIVISION("p/"),
  MULTIPLICATION("*"),
  LOG("log"),
  PROT_LOG("plog"),
  EXP("exp"),
  SIN("sin"),
  COS("cos"),
  INVERSE("1/"),
  OPPOSITE("_"),
  SQRT("√"),
  SQ("²");
  
  private final String string;

  private Operator(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
  
}
